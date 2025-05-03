package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.internal.getMetadataFromNativeMetadata
import io.github.timortel.kmpgrpc.core.io.internal.CodedInputStreamImpl
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.native.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.writeUByte
import platform.posix.size_t
import platform.posix.uint8_tVar
import kotlin.native.concurrent.Worker
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.Channel as CoroutineChannel

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
    return rpcImplementation(
        channel,
        metadata,
        MethodDescriptor.MethodType.UNARY,
        path,
        flowOf(request),
        responseDeserializer
    ).single()
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
    return rpcImplementation(
        channel,
        metadata,
        MethodDescriptor.MethodType.SERVER_STREAMING,
        path,
        flowOf(request),
        responseDeserializer
    )
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
    return rpcImplementation(
        channel,
        metadata,
        MethodDescriptor.MethodType.CLIENT_STREAMING,
        path,
        requests,
        responseDeserializer
    ).single()
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
    return rpcImplementation(
        channel,
        metadata,
        MethodDescriptor.MethodType.BIDI_STREAMING,
        path,
        requests,
        responseDeserializer
    )
}

@OptIn(ExperimentalUuidApi::class)
private fun <REQ : Message, RES : Message> rpcImplementation(
    channel: Channel,
    metadata: Metadata,
    methodType: MethodDescriptor.MethodType,
    path: String,
    requests: Flow<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    val methodDescriptor = MethodDescriptor(fullMethodName = path, methodType = methodType)
    val nativeMethodType = when (methodType) {
        MethodDescriptor.MethodType.UNARY -> UNARY
        MethodDescriptor.MethodType.SERVER_STREAMING -> SERVER_STREAMING
        MethodDescriptor.MethodType.CLIENT_STREAMING -> CLIENT_STREAMING
        MethodDescriptor.MethodType.BIDI_STREAMING -> BIDI_STREAMING
    }

    return channelFlow {
        val actualMetadata = channel.interceptor?.onStart(methodDescriptor, metadata) ?: metadata

        val context = create_client_context()
        actualMetadata.entries.forEach { entry ->
            client_context_add_metadata(context, entry.key, entry.value)
        }

        val callData = create_call_data()

        try {
            val contextData = CallContextData(
                writeReadyChannel = CoroutineChannel(
                    capacity = 1,
                    onUndeliveredElement = {
                        throw StatusException.internal("Write ready channel could not keep up!")
                    }
                ),
                messageReceivedChannel = CoroutineChannel(
                    capacity = CoroutineChannel.UNLIMITED
                ),
                responseDeserializer = responseDeserializer
            )

            rpc_impl(
                channel = channel.channel,
                client_context = context,
                method_name = path,
                call_data = callData,
                type = nativeMethodType,
                data = StableRef.create(contextData).asCPointer(),
                on_message_received = staticCFunction { data, byteBuffer ->
                    val callContextData = data!!.asStableRef<CallContextData<RES>>().get()

                    val receivedMessage = readMessageFromByteBuffer(
                        get_byte_buffer_data(byteBuffer),
                        callContextData.responseDeserializer
                    )
                    callContextData.messageReceivedChannel.trySend(receivedMessage)
                },
                on_write_done = staticCFunction { data ->
                    val callContextData = data!!.asStableRef<CallContextData<RES>>().get()

                    // Write is done, so we can clean up the write buffer
                    destroy_byte_buffer(callContextData.currentWriteBuffer)

                    callContextData.writeReadyChannel.trySend(Unit)
                },
                on_initial_metadata_received = staticCFunction { data ->

                },
                on_done = staticCFunction { data, callStatus ->
                    val callContextData = data!!.asStableRef<CallContextData<RES>>().get()
                    callContextData.messageReceivedChannel.close()

                    val nativeMessage = call_status_message(callStatus)
                    val status = Status(
                        code = Code.getCodeForValue(call_status_code(callStatus)),
                        statusMessage = nativeMessage?.toKString().orEmpty()
                    )

                    if (nativeMessage != null) {
                        nativeHeap.free(nativeMessage)
                    }

                    callContextData.callStatusCompletable.complete(status)
                }
            )

            val writeJob = launch {
                requests.collect { request ->
                    val actualMessage = channel.interceptor?.onSendMessage(methodDescriptor, request) ?: request
                    val data = actualMessage.serialize().toUByteArray()

                    val requestBuffer = if (data.isEmpty()) {
                        create_byte_buffer(null, 0u)
                    } else {
                        create_byte_buffer(data.usePinned { it.addressOf(0) }, data.size.convert())
                    }

                    // Wait for a write to be ready
                    contextData.writeReadyChannel.receive()

                    contextData.currentWriteBuffer = requestBuffer
                    write_message(callData, requestBuffer)
                }

                signal_client_writing_end(callData)
            }

            // Initially, we are ready for the first send operation
            contextData.writeReadyChannel.send(Unit)

            contextData
                .messageReceivedChannel
                .receiveAsFlow()
                .map { channel.interceptor?.onReceiveMessage(methodDescriptor, it) ?: it }
                .collect { send(it) }

            if (writeJob.isActive) {
                writeJob.cancel("The call has been finalized")
            }

            val resultStatus = contextData.callStatusCompletable.await()

            val nativeMetadata = client_context_get_trailing_metadata(context)

            try {
                val finalMetadata = getMetadataFromNativeMetadata(nativeMetadata)

                val actualResultStatus =
                    channel.interceptor?.onClose(methodDescriptor, resultStatus, Metadata.of(finalMetadata))
                        ?.first
                        ?: resultStatus

                if (actualResultStatus.code != Code.OK) {
                    throw StatusException(actualResultStatus, null)
                }
            } finally {
                metadata_const_destroy(nativeMetadata)
            }
        } finally {
            destroy_client_context(context)
            destroy_call_data(callData)
        }
    }
}

private data class CallContextData<RES : Message>(
    val writeReadyChannel: CoroutineChannel<Unit>,
    val messageReceivedChannel: CoroutineChannel<RES>,
    val responseDeserializer: MessageDeserializer<RES>,
    var currentWriteBuffer: CValuesRef<cnames.structs.byte_buffer>? = null,
    val callStatusCompletable: CompletableDeferred<Status> = CompletableDeferred()
)

private fun <RES : Message> readMessageFromByteBuffer(
    byteBufferData: CPointer<cnames.structs.byte_buffer_data>?,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    val responseDataPointer = get_byte_buffer_data_data(byteBufferData)
    val responseDataSize = get_byte_buffer_data_size(byteBufferData)

    return if (responseDataSize == 0uL) {
        responseDeserializer.deserialize(byteArrayOf())
    } else if (responseDataPointer != null) {
        val rawSource = MemoryRawSource(responseDataPointer, responseDataSize)

        responseDeserializer.deserialize(CodedInputStreamImpl(rawSource.buffered()))
    } else {
        throw StatusException.internal("Received data but no memory data was given")
    }
}
