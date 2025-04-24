package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.external.RpcError
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlin.time.ExperimentalTime

private const val KEY_GRPC_STATUS = "grpc-status"
private const val KEY_GRPC_MESSAGE = "grpc-message"

/**
 * Executes the unary given call and maps [RpcError]s to [StatusException]s.
 *
 * @param metadata The metadata that should be sent with this call.
 * @return The result of the call
 * @throws StatusException if an RpcError is caught, wrapping the error details into a KMStatusException.
 * @throws Exception if any other exception is caught during execution.
 */
suspend fun <Request : Message, Response : Message> unaryCallImplementation(
    channel: Channel,
    path: String,
    request: Request,
    responseDeserializer: MessageDeserializer<Response>,
    metadata: Metadata
): Response {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.UNARY
    )

    return unaryResponseCallBaseImplementation(channel) {
        // Taken from https://github.com/grpc/grpc-kotlin/blob/6f774052d1d6923f8af2e0023886d69949b695ee/stub/src/main/java/io/grpc/kotlin/Helpers.kt#L57
        flow {
            var receivedResponse = false
            grpcImplementation(channel, path, request, responseDeserializer, metadata, methodDescriptor)
                .collect { response ->
                    if (!receivedResponse) {
                        receivedResponse = true
                        emit(response)
                    } else {
                        throw StatusException.InternalOnlyExpectedOneElement
                    }
                }

            if (!receivedResponse) {
                throw StatusException.InternalExpectedAtLeastOneElement
            }
        }
            .single()
    }
}

/**
 * Handles server-side streaming calls by converting a dynamic call response into a Flow of type JS_RESPONSE.
 *
 * @param metadata The metadata that should be sent with this call.
 * @return A [Flow] instance emitting responses of type JS_RESPONSE, or errors if the streaming call fails.
 */
fun <Request : Message, Response : Message> serverSideStreamingCallImplementation(
    channel: Channel,
    path: String,
    request: Request,
    responseDeserializer: MessageDeserializer<Response>,
    metadata: Metadata
): Flow<Response> {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.SERVER_STREAMING
    )

    return streamingResponseCallBaseImplementation(
        channel = channel,
        responseFlow = grpcImplementation(channel, path, request, responseDeserializer, metadata, methodDescriptor)
    )
}

@OptIn(ExperimentalTime::class)
private fun <Request : Message, Response : Message> grpcImplementation(
    channel: Channel,
    path: String,
    request: Request,
    deserializer: MessageDeserializer<Response>,
    metadata: Metadata,
    methodDescriptor: MethodDescriptor
): Flow<Response> {
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
