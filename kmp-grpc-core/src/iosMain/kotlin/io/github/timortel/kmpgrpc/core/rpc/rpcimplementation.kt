package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.io.internal.CodedInputStreamImpl
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import kotlinx.cinterop.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import io.github.timortel.kmpgrpc.native.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.writeUByte
import platform.posix.size_t
import platform.posix.uint8_tVar
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

private const val GRPC_ERROR_DOMAIN = "io.grpc"

private const val INVALID_UNKNOWN_DESCRIPTION_1 = "UNKNOWN {grpc_status:0, grpc_message:\"\"}"
private const val INVALID_UNKNOWN_DESCRIPTION_2 = "UNKNOWN {grpc_message:\"\", grpc_status:0}"

/**
 * Executes a unary gRPC call using the provided channel and settings.
 *
 * This method sends a single request to the gRPC server and waits for a single response.
 * The call implementation handles serialization/deserialization of messages and performs
 * necessary channel settings and interceptor applications. It also supports cancellation handling.
 *
 * @param channel The gRPC channel used to execute the call.
 * @param callOptions Options for configuring the behavior of the call, such as deadlines or metadata.
 * @param path The full method path of the gRPC service being invoked.
 * @param request The request message to be sent to the server.
 * @param requestDeserializer A deserializer to help parse serialized request objects into a protocol buffer message.
 * @param responseDeserializer A deserializer to parse serialized response data into the appropriate protocol buffer object.
 * @return The response message from the server as an instance of the specified type.
 * @throws StatusException If the gRPC call encounters an error or the server returns a failure status.
 * @throws CancellationException If the coroutine's job is canceled.
 */
@Throws(StatusException::class, CancellationException::class)
suspend fun <REQ : Message, RES : Message> unaryCallImplementation(
    channel: Channel,
    metadata: Metadata,
    path: String,
    request: REQ,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    val requestData = request.serialize().toUByteArray()

    val context = create_client_context()
    val stub = create_stub(channel.channel)

    val requestBuffer = if (requestData.isEmpty()) {
        create_byte_buffer(null, 0u)
    } else {
        create_byte_buffer(requestData.usePinned { it.addressOf(0) }, requestData.size.convert())
    }

    val responseBuffer = create_empty_byte_buffer()

    return try {
        val responseStatus: Status = suspendCancellableCoroutine { continuation ->
            unary_rpc(
                stub = stub,
                client_context = context,
                method_name = path,
                request_buffer = requestBuffer,
                response_buffer = responseBuffer,
                data = StableRef.create(continuation).asCPointer(),
                callback = staticCFunction { status, message, data ->
                    println("Callback complete")
                    val continuation = data!!.asStableRef<Continuation<Status>>().get()

                    continuation.resume(
                        Status(
                            Code.getCodeForValue(status),
                            message?.toKString().orEmpty(),
                        )
                    )
                }
            )

            continuation.invokeOnCancellation {
                cancel_call(context)
            }
        }

        println("Reading status")

        if (responseStatus.code != Code.OK) {
            throw StatusException(
                status = responseStatus,
                cause = null
            )
        }

        val responseBufferData = get_byte_buffer_data(responseBuffer)

        println("Got response data")

        try {
            val responseDataPointer = get_byte_buffer_data_data(responseBufferData)
            val responseDataSize = get_byte_buffer_data_size(responseBufferData)

            if (responseDataSize == 0uL) {
                responseDeserializer.deserialize(byteArrayOf())
            } else if (responseDataPointer != null) {
                val rawSource = MemoryRawSource(responseDataPointer, responseDataSize)

                responseDeserializer.deserialize(CodedInputStreamImpl(rawSource.buffered()))
            } else {
                throw StatusException.internal("Received data but no memory data was given")
            }
        } finally {
            destroy_byte_buffer_data(responseBufferData)
        }
    } finally {
        println("Cleanup")
        destroy_stub(stub)
        destroy_byte_buffer(requestBuffer)
        destroy_byte_buffer(responseBuffer)
    }
}

private class MemoryRawSource(
    val pointer: CPointer<uint8_tVar>,
    val size: size_t
) : RawSource {
    var position = 0uL

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        if (position >= size) return -1L

        val actualReadCount = minOf(byteCount.toULong(), size - position)

        for (i in 0uL until actualReadCount) {
            sink.writeUByte(pointer[i.toInt()])
        }

        position += actualReadCount
        return actualReadCount.toLong()
    }

    override fun close() = Unit
}

/**
 * Handles server-side streaming gRPC calls by setting up a streaming call, writing the request, finishing the call,
 * and returning a flow of responses from the server. It also supports cancellation handling.
 *
 * @param REQ The type of the request message, which must extend [Message].
 * @param RES The type of the response message, which must extend [Message].
 * @param channel The gRPC [Channel] used for the call.
 * @param callOptions The [GRPCCallOptions] containing call configuration settings and options.
 * @param path The full method name of the server-side streaming gRPC endpoint.
 * @param request The request message to send to the server.
 * @param requestDeserializer The [MessageDeserializer] responsible for deserializing the request message.
 * @param responseDeserializer The [MessageDeserializer] responsible for deserializing the response messages.
 * @return A [Flow] of response messages from the server, where each emitted item represents a message from the stream.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <REQ : Message, RES : Message> serverSideStreamingCallImplementation(
    channel: Channel,
    metadata: Metadata,
    path: String,
    request: REQ,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    TODO()
}

/**
 * Executes a client-streaming gRPC call, sending a stream of requests to the server and receiving a single response.
 * It also supports cancellation handling.
 *
 * @param REQ The type of the request messages being streamed to the server. Must extend [Message].
 * @param RES The type of the single response message received from the server. Must extend [Message].
 * @param channel The gRPC [Channel] used to communicate with the server.
 * @param callOptions The gRPC call options ([GRPCCallOptions]) used to configure the call.
 * @param path The fully qualified path of the gRPC method being called.
 * @param requests A [Flow] emitting the request messages to be sent as part of the stream.
 * @param requestDeserializer The deserializer used to deserialize the requests sent to the server.
 * @param responseDeserializer The deserializer used to deserialize the response received from the server.
 * @return The single response from the server of type [RES].
 * @throws StatusException If the call fails due to a gRPC status-related error.
 * @throws CancellationException If the coroutine or the call is canceled.
 */
suspend fun <REQ : Message, RES : Message> clientStreamingCallImplementation(
    channel: Channel,
    metadata: Metadata,
    path: String,
    requests: Flow<REQ>,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    TODO()
}

/**
 * Implements a bidirectional streaming gRPC call, allowing for simultaneous streaming of requests and responses.
 * It also supports cancellation handling.
 *
 * @param channel The gRPC communication channel used to establish the call.
 * @param callOptions The configuration options for the gRPC call, including timeout and metadata.
 * @param path The fully qualified path of the gRPC method being called.
 * @param requests A flow emitting the request messages to be sent to the server.
 * @param requestDeserializer A deserializer for the request messages to handle serialization details.
 * @param responseDeserializer A deserializer for the response messages to handle deserialization details.
 * @return A flow of response messages received from the server.
 */
fun <REQ : Message, RES : Message> bidiStreamingCallImplementation(
    channel: Channel,
    metadata: Metadata,
    path: String,
    requests: Flow<REQ>,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    TODO()
}

