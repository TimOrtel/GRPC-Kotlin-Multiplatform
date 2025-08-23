package io.github.timortel.kmpgrpc.core.rpc

import cnames.structs.RustMetadata
import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.internal.MemoryRawSource
import io.github.timortel.kmpgrpc.core.io.internal.CodedInputStreamImpl
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.metadata.Entry
import io.github.timortel.kmpgrpc.core.metadata.Key
import io.github.timortel.kmpgrpc.core.metadata.Metadata
import io.github.timortel.kmpgrpc.native.*
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.io.buffered
import kotlinx.coroutines.channels.Channel as CoroutineChannel

/**
 * Executes a unary gRPC call using the provided channel and settings.
 *
 * This method sends a single request to the gRPC server and waits for a single response.
 * The call implementation handles serialization/deserialization of messages and performs
 * necessary channel settings and interceptor applications. It also supports cancellation handling.
 *
 * @param channel The gRPC channel used to execute the call.
 * @param callOptions The gRPC call options used to configure the call.
 * @param path The full method path of the gRPC service being invoked.
 * @param request The request message to be sent to the server.
 * @param responseDeserializer A deserializer to parse serialized response data into the appropriate protocol buffer object.
 * @return The response message from the server as an instance of the specified type.
 * @throws StatusException If the gRPC call encounters an error or the server returns a failure status.
 * @throws CancellationException If the coroutine's job is canceled.
 */
@InternalKmpGrpcApi
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
        ).singleOrStatus()
    }
}

/**
 * Handles server-side streaming gRPC calls by setting up a streaming call, writing the request, finishing the call,
 * and returning a flow of responses from the server. It also supports cancellation handling.
 *
 * @param REQ The type of the request message, which must extend [Message].
 * @param RES The type of the response message, which must extend [Message].
 * @param channel The gRPC [Channel] used for the call.
 * @param callOptions The gRPC call options used to configure the call.
 * @param path The full method name of the server-side streaming gRPC endpoint.
 * @param request The request message to send to the server.
 * @param responseDeserializer The [MessageDeserializer] responsible for deserializing the response messages.
 * @return A [Flow] of response messages from the server, where each emitted item represents a message from the stream.
 */
@InternalKmpGrpcApi
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
 * @param callOptions The gRPC call options used to configure the call.
 * @param path The fully qualified path of the gRPC method being called.
 * @param requests A [Flow] emitting the request messages to be sent as part of the stream.
 * @param responseDeserializer The deserializer used to deserialize the response received from the server.
 * @return The single response from the server of type [RES].
 * @throws StatusException If the call fails due to a gRPC status-related error.
 * @throws CancellationException If the coroutine or the call is canceled.
 */
@InternalKmpGrpcApi
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
        ).singleOrStatus()
    }
}

/**
 * Implements a bidirectional streaming gRPC call, allowing for simultaneous streaming of requests and responses.
 * It also supports cancellation handling.
 *
 * @param channel The gRPC communication channel used to establish the call.
 * @param callOptions The gRPC call options used to configure the call.
 * @param path The fully qualified path of the gRPC method being called.
 * @param requests A flow emitting the request messages to be sent to the server.
 * @param responseDeserializer A deserializer for the response messages to handle deserialization details.
 * @return A flow of response messages received from the server.
 */
@InternalKmpGrpcApi
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

/**
 * rpc implementation that delegates the heavy lifting to rust/tonic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun <REQ : Message, RES : Message> rpcImplementation(
    channel: Channel,
    callOptions: CallOptions,
    methodType: MethodDescriptor.MethodType,
    path: String,
    requests: Flow<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    val methodDescriptor = MethodDescriptor(fullMethodName = path, methodType = methodType)

    val rpcFlow = channelFlow {
        if (channel.isShutdown.value) throw StatusException.CancelledDueToShutdown

        channel.registerRpc()

        val actualMetadata = channel.interceptor.onStart(methodDescriptor, callOptions.metadata)

        val contextData = CallContextData(responseDeserializer, channel)
        val requestChannel = request_channel_create()
        val callMetadata = createRustMetadata(actualMetadata)

        val callContextData = StableRef.create(contextData)

        val sendJob = launch {
            try {
                requests
                    .map { channel.interceptor.onSendMessage(methodDescriptor, it) }
                    .collect { req ->
                        while (true) {
                            val sendResult =
                                request_channel_send(requestChannel, StableRef.create(req).asCPointer())

                            when (sendResult) {
                                Ok -> break
                                Closed, NoSender -> {
                                    return@collect
                                }

                                Full -> {
                                    // The buffer is full, give some time to write and try again
                                    delay(5)
                                }

                                else -> throw IllegalStateException("Unknown send result $sendResult")
                            }
                        }
                    }
            } finally {
                request_channel_signal_end(requestChannel)
                request_channel_free(requestChannel)
            }
        }

        val receiveMessagesJob = launch {
            contextData
                .messageReceiveChannel
                .receiveAsFlow()
                .map { channel.interceptor.onReceiveMessage(methodDescriptor, it) }
                .collect {
                    send(it)
                }
        }

        val waitForInitialMetadataDeferred = async {
            val initialMetadata = contextData.initialMetadataCompletable.await()

            val metadata = channel.interceptor.onReceiveHeaders(methodDescriptor, initialMetadata)

            // Check that status is ok, otherwise throw
            extractStatusFromMetadataAndVerify(metadata)

            metadata
        }

        val waitForDoneJob = launch {
            val resultStatus = contextData.callStatusCompletable.await()

            if (resultStatus.status != null && resultStatus.status.code != Code.OK) {
                throw StatusException(resultStatus.status, null)
            }

            try {
                // Get the initial metadata or empty if we did not receive any
                val initialMetadata = try {
                    waitForInitialMetadataDeferred.getCompleted()
                } catch (_: IllegalStateException) {
                    Metadata.empty()
                }

                val finalMetadata = initialMetadata + resultStatus.trailers
                extractStatusFromMetadataAndVerify(finalMetadata) { statusFromMetadata ->
                    channel.interceptor.onClose(methodDescriptor, statusFromMetadata, finalMetadata).first
                }
            } finally {
                close()
            }
        }

        val taskHandle = rpc_implementation(
            channel = channel.channel,
            path = path,
            metadata = callMetadata,
            request_channel = requestChannel,
            user_data = callContextData.asCPointer(),
            serialize_request = staticCFunction { message ->
                val messageRef = message!!.asStableRef<Message>()
                try {
                    val msg = messageRef.get()

                    if (msg.requiredSize == 0) {
                        c_byte_array_create(
                            data = null,
                            ptr = null,
                            len = 0uL,
                            free = staticCFunction { _ -> }
                        )
                    } else {
                        val array = msg.serialize().toUByteArray()

                        val pinnedArray = array.pin()

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
                if (data == null) return@staticCFunction null

                val context = data.asStableRef<CallContextData<RES>>().get()
                val messageDeserializer = context.deserializer

                val message = if (length > 0uL && ptr != null) {
                    val source = MemoryRawSource(ptr, length)

                    messageDeserializer.deserialize(CodedInputStreamImpl(source.buffered()))
                } else {
                    messageDeserializer.deserialize(byteArrayOf())
                }

                StableRef.create(message).asCPointer()
            },
            on_message_received = staticCFunction { data, message ->
                if (data == null) return@staticCFunction

                val data = data.asStableRef<CallContextData<RES>>().get()

                val msg = message!!.asStableRef<Message>()

                try {
                    @Suppress("UNCHECKED_CAST")
                    data.messageReceiveChannel.trySend(msg.get() as RES)
                } finally {
                    msg.dispose()
                }
            },
            on_initial_metadata_received = staticCFunction { data, initialMetadata ->
                if (data == null) return@staticCFunction

                try {
                    val data = data.asStableRef<CallContextData<RES>>().get()

                    data.initialMetadataCompletable.complete(convertRustMetadata(initialMetadata))
                } finally {
                    metadata_free(initialMetadata)
                }
            },
            on_done = staticCFunction { data, code, message, metadata, trailers ->
                if (data == null) return@staticCFunction
                val dataRef = data.asStableRef<CallContextData<RES>>()

                try {
                    val status: Status? = if (code == -1) {
                        // This means we finalized with trailers. Read status from trailers.
                        null
                    } else {
                        Status(
                            code = Code.getCodeForValue(code),
                            statusMessage = message?.toKString().orEmpty()
                        )
                    }

                    dataRef.get().callStatusCompletable.complete(
                        CallCompletionData(
                            status = status,
                            trailers = convertRustMetadata(trailers)
                        )
                    )
                } finally {
                    string_free(message)
                    metadata_free(metadata)
                    metadata_free(trailers)

                    // Try to unregisterRpc in any case.
                    try {
                        dataRef.get().channel.unregisterRpc()
                    } finally {
                        dataRef.dispose()
                    }
                }
            }
        )

        awaitClose {
            val message = "The call has been finalized"

            contextData.close()
            sendJob.cancel(message)
            receiveMessagesJob.cancel(message)
            waitForDoneJob.cancel(message)
            waitForInitialMetadataDeferred.cancel(message)

            rpc_task_abort(taskHandle)
        }
    }.flowOn(channel.context)

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
    val channel: Channel,
    val messageReceiveChannel: CoroutineChannel<RES> = CoroutineChannel(capacity = CoroutineChannel.UNLIMITED),
    val callStatusCompletable: CompletableDeferred<CallCompletionData> = CompletableDeferred(),
    val initialMetadataCompletable: CompletableDeferred<Metadata> = CompletableDeferred(),
) {
    fun close() {
        messageReceiveChannel.close()
    }
}

private data class CallCompletionData(
    /**
     * A value of null means that the status should be read from trailers
     */
    val status: Status?,
    val trailers: Metadata
)

private fun createRustMetadata(metadata: Metadata): CPointer<RustMetadata>? {
    return memScoped {
        val asciiCStrings = metadata.entries
            .filterIsInstance<Entry.Ascii>()
            .flatMap { (key, values) ->
                values.flatMap { value -> listOf(key.name, value) }
            }
            .map { it.cstr.ptr } + null

        val binaryEntries = metadata.entries
            .filterIsInstance<Entry.Binary>()

        val metadata = if (binaryEntries.isNotEmpty()) {
            val binaryCStrings = binaryEntries
                // For each value we need a key
                .flatMap { entry -> entry.values.map { entry.key.name } }
                .map { it.cstr.ptr } + null

            val binaryByteArrays: List<Pinned<UByteArray>> = binaryEntries
                .flatMap { entry -> entry.values.map { it.toUByteArray().pin() } }

            val binaryPointers: CValues<CPointerVar<UByteVar>> =
                binaryByteArrays.map { if (it.get().isEmpty()) null else it.addressOf(0) }.toCValues()

            val binarySizesPinned: Pinned<ULongArray> =
                binaryByteArrays.map { it.get().size.toULong() }.toULongArray().pin()
            val binarySizes: CPointer<ULongVar> = binarySizesPinned.addressOf(0)

            val metadata = metadata_create(
                ascii_entries = asciiCStrings.toCValues(),
                binary_keys = binaryCStrings.toCValues(),
                binary_ptrs = binaryPointers,
                binary_sizes = binarySizes
            )

            // Cleanup
            binaryByteArrays.forEach { it.unpin() }
            binarySizesPinned.unpin()

            metadata
        } else {
            metadata_create(asciiCStrings.toCValues(), null, null, null)
        }

        metadata
    }
}

private fun convertRustMetadata(rustMetadata: CPointer<RustMetadata>?): Metadata {
    val entries = buildList {
        val ref: StableRef<MutableList<Entry<*>>> = StableRef.create(this)

        try {
            metadata_iterate(
                metadata = rustMetadata,
                data = ref.asCPointer(),
                block_ascii = staticCFunction { data, key, value ->
                    try {
                        val keyString = key?.toKString() ?: return@staticCFunction
                        val valueString = value?.toKString() ?: return@staticCFunction

                        val list = data!!.asStableRef<MutableList<Entry<*>>>().get()
                        list += Entry.Ascii(Key.AsciiKey(keyString), setOf(valueString))
                    } finally {
                        string_free(key)
                        string_free(value)
                    }
                },
                block_binary = staticCFunction { data, key, valuePtr, valueSize ->
                    try {
                        val keyString = key?.toKString() ?: return@staticCFunction
                        val array = if(valueSize == 0uL || valuePtr == null) {
                            byteArrayOf()
                        } else {
                            ByteArray(valueSize.toInt()) { i ->
                                valuePtr[i].toByte()
                            }
                        }

                        val list = data!!.asStableRef<MutableList<Entry<*>>>().get()
                        list += Entry.Binary(Key.BinaryKey(keyString), setOf(array))
                    } finally {
                        string_free(key)
                    }
                }
            )
        } finally {
            ref.dispose()
        }
    }

    return Metadata.of(entries)
}
