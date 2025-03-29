package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import cocoapods.GRPCClient.GRPCCall2
import cocoapods.GRPCClient.GRPCCallOptions
import cocoapods.GRPCClient.GRPCResponseHandlerProtocol
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMCode
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatus
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatusException
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
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

/**
 * Perform a unary rpc call as a suspending function. Uses [GRPCCall2] for the actual call.
 */
@Throws(KMStatusException::class, CancellationException::class)
suspend fun <REQ : KMMessage, RES : KMMessage> unaryCallImplementation(
    channel: KMChannel,
    callOptions: GRPCCallOptions,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES, NSData>
): RES {
    val data = request.serialize()

    return suspendCancellableCoroutine { continuation ->
        val handler = UnaryCallHandler(
            onDone = { data, error ->
                when {
                    error != null -> {
                        val exception =
                            KMStatusException(
                                KMStatus(KMCode.getCodeForValue(error.code.toInt()), error.description ?: "No description"),
                                null
                            )

                        continuation.resumeWithException(exception)
                    }
                    data != null -> {
                        continuation.resume(responseDeserializer.deserialize(data as NSData))
                    }
                    else -> {
                        val status = KMStatus(KMCode.UNKNOWN, "Call closed without error and without a response")
                        continuation.resumeWithException(KMStatusException(status, null))
                    }
                }
            }
        )

        val call = GRPCCall2(
            requestOptions = channel.buildRequestOptions(path),
            responseHandler = handler,
            callOptions = channel.applyToCallOptions(callOptions)
        )

        call.start()
        call.writeData(data)
        call.finish()

        continuation.invokeOnCancellation { call.cancel() }
    }
}

/**
 * Performs a server side stream call and returns a [Flow] that emits whenever we receive a new message from the server.
 * Uses [GRPCCall2] for the actual call.
 */
fun <REQ : KMMessage, RES : KMMessage> serverSideStreamingCallImplementation(
    channel: KMChannel,
    callOptions: GRPCCallOptions,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES, NSData>
): Flow<RES> {
    return callbackFlow {
        val handler = StreamingCallHandler(
            onReceive = { data ->
                val msg = responseDeserializer.deserialize(data as NSData)

                trySend(msg)
            },
            onDone = { error ->
                if (error != null) {
                    val exception = KMStatusException(
                        KMStatus(KMCode.getCodeForValue(error.code.toInt()), error.description ?: "No description"),
                        null
                    )

                    close(exception)
                } else {
                    close()
                }
            }
        )

        val call = GRPCCall2(channel.buildRequestOptions(path), handler, channel.applyToCallOptions(callOptions))

        call.start()
        call.writeData(request.serialize())
        call.finish()

        awaitClose {
            call.cancel()
        }
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
