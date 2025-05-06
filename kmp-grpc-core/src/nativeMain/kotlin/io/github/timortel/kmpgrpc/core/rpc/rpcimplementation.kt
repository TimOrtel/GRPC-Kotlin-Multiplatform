package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.internal.MemoryRawSource
import io.github.timortel.kmpgrpc.core.io.internal.CodedInputStreamImpl
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.native.BIDI_STREAMING
import io.github.timortel.kmpgrpc.native.CLIENT_STREAMING
import io.github.timortel.kmpgrpc.native.SERVER_STREAMING
import io.github.timortel.kmpgrpc.native.UNARY
import io.github.timortel.kmpgrpc.nativerust.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.io.buffered
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
    callOptions: CallOptions,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    return unaryResponseCallBaseImplementation(channel) {
        rpcImplementation(
            channel = channel,
            callOptions = callOptions,
            methodType = MethodDescriptor.MethodType.UNARY,
            path = path,
            requests = flowOf(request),
            responseDeserializer = responseDeserializer
        ).single()
    }
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
    callOptions: CallOptions,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    return streamingResponseCallBaseImplementation(
        channel = channel,
        responseFlow = rpcImplementation(
            channel = channel,
            callOptions = callOptions,
            methodType = MethodDescriptor.MethodType.SERVER_STREAMING,
            path = path,
            requests = flowOf(request),
            responseDeserializer = responseDeserializer
        )
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
    callOptions: CallOptions,
    path: String,
    requests: Flow<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    return unaryResponseCallBaseImplementation(channel) {
        rpcImplementation(
            channel = channel,
            callOptions = callOptions,
            methodType = MethodDescriptor.MethodType.CLIENT_STREAMING,
            path = path,
            requests = requests,
            responseDeserializer = responseDeserializer
        ).single()
    }
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
    callOptions: CallOptions,
    path: String,
    requests: Flow<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    return streamingResponseCallBaseImplementation(
        channel = channel,
        responseFlow = rpcImplementation(
            channel = channel,
            callOptions = callOptions,
            methodType = MethodDescriptor.MethodType.BIDI_STREAMING,
            path = path,
            requests = requests,
            responseDeserializer = responseDeserializer
        )
    )
}

private fun <REQ : Message, RES : Message> rpcImplementation(
    channel: Channel,
    callOptions: CallOptions,
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

    val metadata = callOptions.metadata

    val rpcFlow = channelFlow {
        channel.registerRpc()

        val contextData = CallContextData(responseDeserializer)
        val requestChannel = request_channel_create()

        val callContextData = StableRef.create(contextData)

        try {
//            val actualMetadata = channel.interceptor?.onStart(methodDescriptor, metadata) ?: metadata
//
//            actualMetadata.entries.forEach { entry ->
//                client_context_add_metadata(context, entry.key, entry.value)
//            }

            val sendJob = launch {
                requests.collect { req ->
                    contextData.onMessageWrittenChannel.receive()
                    println("Sending")
                    request_channel_send(requestChannel, StableRef.create(req).asCPointer())
                }

                // When done, still wait for the writing to have been completed, then close
                contextData.onMessageWrittenChannel.receive()

                request_channel_signal_end(requestChannel)
            }

            val receiveMessagesJob = launch {
                contextData
                    .messageReceiveChannel
                    .receiveAsFlow()
//                    .map { channel.interceptor?.onReceiveMessage(methodDescriptor, it) ?: it }
                    .collect {
                        println("rpcImplementation - Received message")
                        send(it)
                    }
            }

            val waitForDoneJob = launch {
                val resultStatus = contextData.callStatusCompletable.await()
                println("rpcImplementation - done received")

//                val nativeMetadata = client_context_get_trailing_metadata(context)

                try {
//                    val finalMetadata = getMetadataFromNativeMetadata(nativeMetadata)
//
//                    val actualResultStatus =
//                        channel.interceptor?.onClose(methodDescriptor, resultStatus, Metadata.of(finalMetadata))
//                            ?.first
//                            ?: resultStatus

//                    if (actualResultStatus.code != Code.OK) {
//                        throw StatusException(actualResultStatus, null)
//                    }
                } finally {
                    println("rpcImplementation - closing call")

//                    metadata_const_destroy(nativeMetadata)

                    close()
                }
            }

            rpc_implementation(
                channel = channel.channel,
                path = path,
                request_channel = requestChannel,
                user_data = callContextData.asCPointer(),
                serialize_request = staticCFunction { message ->
                    println("serialize_request - start")
                    val messageRef = message!!.asStableRef<Message>()
                    try {
                        val msg = messageRef.get()

                        if (msg.requiredSize == 0) {
                            c_byte_array_create(
                                data = null,
                                ptr = null,
                                len = 0uL,
                                free = staticCFunction { _ ->
                                    println("MESSAGE WRITTEN")
                                }
                            )
                        } else {
                            val array = msg.serialize().toUByteArray()

                            val pinnedArray = array.pin()
                            println("serialize_request - pinned array")

                            c_byte_array_create(
                                data = StableRef.create(pinnedArray).asCPointer(),
                                ptr = pinnedArray.addressOf(0),
                                len = array.size.toULong(),
                                free = staticCFunction { data ->
                                    val ref = data!!.asStableRef<Pinned<UByteArray>>()
                                    ref.get().unpin()
                                    ref.dispose()
                                }
                            )
                        }
                    } finally {
                        messageRef.dispose()
                    }

                },
                deserialize_response = staticCFunction { data, ptr, length ->
                    val context = data!!.asStableRef<CallContextData<RES>>().get()
                    val messageDeserializer = context.deserializer

                    println("Received message")
                    val message = if (ptr != null) {
                        val source = MemoryRawSource(ptr, length)

                        messageDeserializer.deserialize(CodedInputStreamImpl(source.buffered()))
                    } else {
                        messageDeserializer.deserialize(byteArrayOf())
                    }

                    StableRef.create(message).asCPointer()
                },
                on_message_received = staticCFunction { data, message ->
                    println("Received message")
                    val data = data!!.asStableRef<CallContextData<RES>>().get()

                    val msg = message!!.asStableRef<Message>()

                    @Suppress("UNCHECKED_CAST")
                    data.messageReceiveChannel.trySend(msg.get() as RES)
                    msg.dispose()
                },
                on_message_written = staticCFunction { data ->
                    val data = data!!.asStableRef<CallContextData<RES>>().get()
                    data.onMessageWrittenChannel.trySend(Unit)
                },
                on_initial_metadata_received = staticCFunction { data, initialMetadata ->
                    val data = data!!.asStableRef<CallContextData<RES>>().get()

                },
                on_done = staticCFunction { data, code, message, metadata, trailers ->
                    val data = data!!.asStableRef<CallContextData<RES>>().get()

                    val status = Status(
                        code = Code.getCodeForValue(code),
                        statusMessage = message?.toKString().orEmpty()
                    )

                    data.callStatusCompletable.complete(status)
                }
            )

            // Allow first send
            contextData.onMessageWrittenChannel.send(Unit)

            awaitClose {
                println("DONE")

                val message = "The call has been finalized"

                contextData.close()
                sendJob.cancel(message)
                receiveMessagesJob.cancel(message)
                waitForDoneJob.cancel(message)
            }
        } finally {
            channel.unregisterRpc()
            request_channel_free(requestChannel)
            callContextData.dispose()
        }
    }

    return flow {
        if (callOptions.deadlineAfter != null) {
            try {
                withTimeout(callOptions.deadlineAfter) {
                    emitAll(rpcFlow)
                }
            } catch (e: TimeoutCancellationException) {
                throw StatusException.requestTimeout(callOptions.deadlineAfter, e)
            }
        } else {
            emitAll(rpcFlow)
        }
    }
}

private data class CallContextData<RES : Message>(
    val deserializer: MessageDeserializer<RES>,
    val messageReceiveChannel: CoroutineChannel<RES> = CoroutineChannel(capacity = CoroutineChannel.UNLIMITED),
    // Only capacity of 1 is needed, as only 1 write will happen and then we wait for this channel to fire
    val onMessageWrittenChannel: CoroutineChannel<Unit> = CoroutineChannel(capacity = 1),
    val callStatusCompletable: CompletableDeferred<Status> = CompletableDeferred(),
    val initialMetadataCompletable: CompletableDeferred<Metadata> = CompletableDeferred()
) {
    fun close() {
        messageReceiveChannel.close()
        onMessageWrittenChannel.close()
    }
}
