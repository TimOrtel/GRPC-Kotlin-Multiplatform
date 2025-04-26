package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private const val KEY_GRPC_STATUS = "grpc-status"
private const val KEY_GRPC_MESSAGE = "grpc-message"

/**
 * Executes the unary given unary call using Ktor.
 *
 * @param callOptions The callOptions that provide additional configuration to this call.
 * @return The result of the call
 * @throws StatusException if an RpcError is caught, wrapping the error details into a KMStatusException.
 * @throws Exception if any other exception is caught during execution.
 */
suspend fun <Request : Message, Response : Message> unaryCallImplementation(
    channel: Channel,
    path: String,
    request: Request,
    responseDeserializer: MessageDeserializer<Response>,
    callOptions: CallOptions
): Response {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.UNARY
    )

    return unaryResponseCallBaseImplementation(channel) {
        grpcImplementation(channel, path, request, responseDeserializer, callOptions, methodDescriptor)
            .singleOrStatus()
    }
}

/**
 * Handles server-side streaming calls by converting a dynamic call response into a Flow of type JS_RESPONSE.
 *
 * @param callOptions The callOptions that provide additional configuration to this call.
 * @return A [Flow] instance emitting responses of type JS_RESPONSE, or errors if the streaming call fails.
 */
fun <Request : Message, Response : Message> serverSideStreamingCallImplementation(
    channel: Channel,
    path: String,
    request: Request,
    responseDeserializer: MessageDeserializer<Response>,
    callOptions: CallOptions
): Flow<Response> {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.SERVER_STREAMING
    )

    return streamingResponseCallBaseImplementation(
        channel = channel,
        responseFlow = grpcImplementation(channel, path, request, responseDeserializer, callOptions, methodDescriptor)
    )
}

@OptIn(ExperimentalTime::class)
private fun <Request : Message, Response : Message> grpcImplementation(
    channel: Channel,
    path: String,
    request: Request,
    deserializer: MessageDeserializer<Response>,
    callOptions: CallOptions,
    methodDescriptor: MethodDescriptor
): Flow<Response> {
    val metadata = callOptions.metadata

    return flow {
        val actualHeaders = channel.interceptors.foldRight(metadata) { interceptor, currentMetadata ->
            interceptor.onStart(methodDescriptor, currentMetadata)
        }

        val actualRequest = channel.interceptors.foldRight(request) { interceptor, currentRequest ->
            interceptor.onSendMessage(methodDescriptor, currentRequest)
        }

        try {
            channel.client
                .preparePost(channel.connectionString + path) {
                    header("Content-Type", "application/grpc-web+proto")
                    header("X-Grpc-Web", "1")

                    actualHeaders.entries.forEach { entry ->
                        header(entry.key, entry.value)
                    }

                    if (callOptions.deadlineAfter != null) {
                        timeout {
                            requestTimeoutMillis = callOptions.deadlineAfter.toLong(DurationUnit.MILLISECONDS)
                        }
                    }

                    setBody(encodeMessageFrame(actualRequest))
                }
                .execute { response ->
                    val responseMetadata = Metadata.of(
                        response.headers.entries().associate { (key, value) ->
                            key to value.joinToString()
                        }
                    )

                    channel.interceptors.fold(responseMetadata) { currentMetadata, interceptor ->
                        interceptor.onReceiveHeaders(methodDescriptor, metadata)
                    }

                    if (!response.status.isSuccess()) {
                        throw StatusException(
                            Status(Code.UNAVAILABLE, "Unsuccessful http request. HTTP-Code=${response.status}"),
                            null
                        )
                    }

                    val bodyChannel = response.bodyAsChannel()
                    while (!bodyChannel.exhausted()) {
                        val frame = Buffer()
                        ByteArray(5).apply {
                            bodyChannel.readFully(this)
                            frame.write(this)
                        }

                        if (frame.remaining == 0L) break

                        val flag = frame.readByte().toUByte()
                        val length = frame.readInt()

                        val payload = bodyChannel.readRemaining(length.toLong())

                        if (flag == 0.toUByte()) {
                            // data received
                            val receivedMessage = deserializer.deserialize(payload.readByteArray())

                            val actualMessage =
                                channel.interceptors.fold(receivedMessage) { currentMessage, interceptor ->
                                    interceptor.onReceiveMessage(methodDescriptor, currentMessage)
                                }

                            emit(actualMessage)
                        } else if (flag == 0x80.toUByte()) {
                            val headers = decodeHeadersFrame(payload)

                            val rawStatus = headers[KEY_GRPC_STATUS]

                            if (rawStatus != null && rawStatus.toIntOrNull() != null) {
                                val code = Code.getCodeForValue(rawStatus.toInt())
                                val status = Status(
                                    code = code,
                                    statusMessage = headers[KEY_GRPC_MESSAGE].orEmpty()
                                )

                                val (finalStatus, _) = channel.interceptors.fold(
                                    Pair(
                                        status,
                                        metadata
                                    )
                                ) { (currentStatus, currentMetadata), interceptor ->
                                    interceptor.onClose(methodDescriptor, currentStatus, currentMetadata)
                                }

                                if (finalStatus.code != Code.OK) {
                                    throw StatusException(
                                        status = finalStatus,
                                        cause = null
                                    )
                                }
                            }
                        }
                    }
                }
        } catch (e: StatusException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            if (callOptions.deadlineAfter != null) {
                throw StatusException.requestTimeout(callOptions.deadlineAfter, e)
            } else {
                throw StatusException.internal("Unexpected timeout except caught.", e)
            }
        } catch (t: Throwable) {
            throw StatusException(
                status = Status(
                    code = Code.UNAVAILABLE,
                    statusMessage = "Could not create rpc."
                ),
                cause = t
            )
        }
    }
}

private fun encodeMessageFrame(message: Message): ByteArray {
    val sink = Buffer()

    val data = message.serialize()
    val size = data.size
    sink.writeByte(0)
    sink.writeInt(size)
    sink.write(data)

    return sink.readByteArray()
}

private fun decodeHeadersFrame(source: Source): Metadata {
    val metadataString = source.readString()

    val entries = metadataString
        .split("\r\n")
        .filter { entry -> entry.isNotBlank() && entry.count { it == ':' } == 1 }
        .associate { metadataEntry ->
            val key = metadataEntry.substringBefore(':')
            val value = metadataEntry.substringAfter(':')

            key to value
        }

    return Metadata.of(entries)
}
