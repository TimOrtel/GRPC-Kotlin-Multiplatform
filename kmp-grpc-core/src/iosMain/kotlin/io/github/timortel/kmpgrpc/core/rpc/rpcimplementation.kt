package io.github.timortel.kmpgrpc.core.rpc

import cocoapods.GRPCClient.*
import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.internal.CallInterceptorWrapper
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.darwin.dispatch_queue_t
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    callOptions: GRPCCallOptions,
    path: String,
    request: REQ,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.UNARY
    )

    return unaryResponseCallBaseImplementation(channel) {
        suspendCancellableCoroutine { continuation ->
            val handler = UnaryCallHandler(
                responseDeserializer = responseDeserializer,
                onComplete = continuation::resume,
                onCompleteWithError = continuation::resumeWithException,
            )

            val call = GRPCCall2(
                requestOptions = channel.buildRequestOptions(path),
                responseHandler = handler,
                callOptions = channel.applyToCallOptions(
                    injectCallInterceptor(
                        callOptions = callOptions,
                        interceptor = channel.interceptor,
                        methodDescriptor = methodDescriptor,
                        requestDeserializer = requestDeserializer,
                        responseDeserializer = responseDeserializer
                    )
                )
            )

            call.start()
            call.receiveNextMessages(1u)

            call.writeData(request.serializeNative())

            call.finish()

            continuation.invokeOnCancellation { call.cancel() }
        }
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
    callOptions: GRPCCallOptions,
    path: String,
    request: REQ,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.SERVER_STREAMING
    )

    val responseFlow = callbackFlow {
        val call = setupStreamingCall(
            responseDeserializer = responseDeserializer,
            channel = channel,
            path = path,
            callOptions = callOptions,
            methodDescriptor = methodDescriptor,
            requestDeserializer = requestDeserializer
        )

        call.writeData(request.serializeNative())

        call.finish()

        awaitClose {
            call.cancel()
        }
    }

    return streamingResponseCallBaseImplementation(
        channel = channel,
        responseFlow = responseFlow
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
    callOptions: GRPCCallOptions,
    path: String,
    requests: Flow<REQ>,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.CLIENT_STREAMING
    )

    return unaryResponseCallBaseImplementation(channel) {
        coroutineScope {
            val completableDeferred = CompletableDeferred<RES>()

            val handler = UnaryCallHandler(
                responseDeserializer = responseDeserializer,
                onComplete = completableDeferred::complete,
                onCompleteWithError = completableDeferred::completeExceptionally
            )

            val call = GRPCCall2(
                requestOptions = channel.buildRequestOptions(path),
                responseHandler = handler,
                callOptions = channel.applyToCallOptions(
                    injectCallInterceptor(
                        callOptions = callOptions,
                        interceptor = channel.interceptor,
                        methodDescriptor = methodDescriptor,
                        requestDeserializer = requestDeserializer,
                        responseDeserializer = responseDeserializer
                    )
                )
            )

            call.start()

            val sender = launch {
                requests.collect { call.writeData(it.serializeNative()) }
                call.finish()
            }

            val result = completableDeferred.await()

            if (sender.isActive) {
                sender.cancel("Response received from server before all messages have been sent.")
                call.finish()
            }

            result
        }
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
    callOptions: GRPCCallOptions,
    path: String,
    requests: Flow<REQ>,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RES>
): Flow<RES> {
    val methodDescriptor = MethodDescriptor(
        fullMethodName = path,
        methodType = MethodDescriptor.MethodType.BIDI_STREAMING
    )

    val responseFlow = callbackFlow {
        val call = setupStreamingCall(
            responseDeserializer = responseDeserializer,
            channel = channel,
            path = path,
            callOptions = callOptions,
            methodDescriptor = methodDescriptor,
            requestDeserializer = requestDeserializer
        )

        launch {
            requests.collect { call.writeData(it.serializeNative()) }
            call.finish()
        }

        awaitClose {
            call.cancel()
        }
    }

    return streamingResponseCallBaseImplementation(
        channel = channel,
        responseFlow = responseFlow
    )
}

/**
 * Sets up and initiates a streaming gRPC call, creating and returning a [GRPCCall2] instance.
 * This method configures the call with options, interceptors, and handlers to manage the response lifecycle.
 *
 * @return A [GRPCCall2] object representing the streaming gRPC call.
 */
private fun <REQ : Message, RES : Message> ProducerScope<RES>.setupStreamingCall(
    responseDeserializer: MessageDeserializer<RES>,
    channel: Channel,
    path: String,
    callOptions: GRPCCallOptions,
    methodDescriptor: MethodDescriptor,
    requestDeserializer: MessageDeserializer<REQ>
): GRPCCall2 {
    val handler = StreamingCallHandler(
        responseDeserializer = responseDeserializer,
        onReceive = ::trySend,
        onDone = { error ->
            if (error != null) {
                val exception = StatusException(error.asGrpcStatus, null)

                close(exception)
            } else {
                close()
            }
        }
    )

    val call = GRPCCall2(
        requestOptions = channel.buildRequestOptions(path),
        responseHandler = handler,
        callOptions = channel.applyToCallOptions(
            injectCallInterceptor(
                callOptions = callOptions,
                interceptor = channel.interceptor,
                methodDescriptor = methodDescriptor,
                requestDeserializer = requestDeserializer,
                responseDeserializer = responseDeserializer
            )
        )
    )

    call.start()

    return call
}

private fun <REQ : Message, RESP : Message> injectCallInterceptor(
    callOptions: GRPCCallOptions,
    interceptor: CallInterceptor?,
    methodDescriptor: MethodDescriptor,
    requestDeserializer: MessageDeserializer<REQ>,
    responseDeserializer: MessageDeserializer<RESP>
): GRPCCallOptions {
    return if (interceptor != null) {

        val newCallOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
        newCallOptions.setInterceptorFactories(
            listOf<GRPCInterceptorFactoryProtocol>(
                InterceptorFactory(
                    methodDescriptor = methodDescriptor,
                    interceptor = interceptor,
                    requestDeserializer = requestDeserializer,
                    responseDeserializer = responseDeserializer
                )
            )
        )

        newCallOptions
    } else callOptions
}

private class InterceptorFactory<REQ : Message, RES : Message>(
    private val methodDescriptor: MethodDescriptor,
    private val interceptor: CallInterceptor,
    private val requestDeserializer: MessageDeserializer<REQ>,
    private val responseDeserializer: MessageDeserializer<RES>
) : GRPCInterceptorFactoryProtocol, NSObject() {
    override fun createInterceptorWithManager(interceptorManager: GRPCInterceptorManager): GRPCInterceptor {
        return CallInterceptorWrapper(
            methodDescriptor = methodDescriptor,
            interceptor = interceptor,
            requestDeserializer = requestDeserializer,
            responseDeserializer = responseDeserializer,
            interceptorManager = interceptorManager,
            dispatchQueue = interceptorManager.dispatchQueue
        )
    }
}

private class StreamingCallHandler<T : Message>(
    private val responseDeserializer: MessageDeserializer<T>,
    private val onReceive: (data: T) -> Unit,
    private val onDone: (error: NSError?) -> Unit
) :
    NSObject(), GRPCResponseHandlerProtocol {

    override fun dispatchQueue(): dispatch_queue_t = null

    override fun didReceiveData(data: Any) = onReceive(responseDeserializer.deserialize(data as NSData))

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        onDone(error)
    }
}

private class UnaryCallHandler<T : Message>(
    private val responseDeserializer: MessageDeserializer<T>,
    private val onComplete: (T) -> Unit,
    private val onCompleteWithError: (Throwable) -> Unit
) : NSObject(), GRPCResponseHandlerProtocol {

    private var data: Any? = null

    override fun dispatchQueue(): dispatch_queue_t = null

    override fun didReceiveData(data: Any) {
        this.data = data
    }

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        // https://github.com/grpc/grpc/issues/39178
        val isInvalidGrpcError = error != null && error.domain == GRPC_ERROR_DOMAIN && error.code == 2L
                && (error.description.orEmpty().contains(INVALID_UNKNOWN_DESCRIPTION_1) ||
                error.description.orEmpty().contains(INVALID_UNKNOWN_DESCRIPTION_2))

        when {
            error != null && !isInvalidGrpcError -> {
                val exception =
                    StatusException(error.asGrpcStatus, null)

                onCompleteWithError(exception)
            }

            data != null -> {
                onComplete(responseDeserializer.deserialize(data as NSData))
            }

            else -> {
                val status = Status(Code.UNKNOWN, "Call closed without error and without a response")
                onCompleteWithError(StatusException(status, null))
            }
        }
    }
}
