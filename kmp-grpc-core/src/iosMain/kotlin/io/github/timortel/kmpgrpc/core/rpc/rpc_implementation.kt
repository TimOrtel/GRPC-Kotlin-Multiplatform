package io.github.timortel.kmpgrpc.core.rpc

import cocoapods.GRPCClient.*
import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.internal.CallInterceptorWrapper
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
 * Perform a unary rpc call as a suspending function. Uses [GRPCCall2] for the actual call.
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

    return suspendCancellableCoroutine { continuation ->
        val handler = UnaryCallHandler(
            onDone = { data, error ->
                // https://github.com/grpc/grpc/issues/39178
                val isInvalidGrpcError = error != null && error.domain == GRPC_ERROR_DOMAIN && error.code == 2L
                        && (error.description.orEmpty().contains(INVALID_UNKNOWN_DESCRIPTION_1) ||
                        error.description.orEmpty().contains(INVALID_UNKNOWN_DESCRIPTION_2))

                when {
                    error != null && !isInvalidGrpcError -> {
                        val exception =
                            StatusException(error.asGrpcStatus, null)

                        continuation.resumeWithException(exception)
                    }

                    data != null -> {
                        continuation.resume(responseDeserializer.deserialize(data as NSData))
                    }

                    else -> {
                        val status = Status(Code.UNKNOWN, "Call closed without error and without a response")
                        continuation.resumeWithException(StatusException(status, null))
                    }
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
        call.receiveNextMessages(1u)

        call.writeData(request.serializeNative())

        call.finish()

        continuation.invokeOnCancellation { call.cancel() }
    }
}

/**
 * Performs a server side stream call and returns a [Flow] that emits whenever we receive a new message from the server.
 * Uses [GRPCCall2] for the actual call.
 */
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

    return callbackFlow {
        val handler = StreamingCallHandler(
            onReceive = { data ->
                val msg = responseDeserializer.deserialize(data as NSData)

                trySend(msg)
            },
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

        call.writeData(request.serializeNative())

        call.finish()

        awaitClose {
            call.cancel()
        }
    }
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

private class StreamingCallHandler(
    private val onReceive: (data: Any) -> Unit,
    private val onDone: (error: NSError?) -> Unit
) :
    NSObject(), GRPCResponseHandlerProtocol {

    override fun dispatchQueue(): dispatch_queue_t = null

    override fun didReceiveData(data: Any) = onReceive(data)

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        onDone(error)
    }
}

private class UnaryCallHandler(
    private val onDone: (data: Any?, error: NSError?) -> Unit
) : NSObject(), GRPCResponseHandlerProtocol {

    private var data: Any? = null

    override fun dispatchQueue(): dispatch_queue_t = null

    override fun didReceiveData(data: Any) {
        this.data = data
    }

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        onDone(data, error)
    }
}
