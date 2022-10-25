package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import cocoapods.GRPCClient.GRPCCall2
import cocoapods.GRPCClient.GRPCCallOptions
import cocoapods.GRPCClient.GRPCMutableCallOptions
import cocoapods.GRPCClient.GRPCResponseHandlerProtocol
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMCode
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatus
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatusException
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.darwin.*
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Throws(KMStatusException::class, CancellationException::class)
suspend fun <REQ : KMMessage, RES : KMMessage> unaryCallImplementation(
    channel: KMChannel,
    callOptions: GRPCCallOptions,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES, NSData>
): RES {
    val data = request.serialize()

    return suspendCoroutine { continuation ->
        val handler = CallHandler(
            onReceive = { data: Any ->
                val response = responseDeserializer.deserialize(data as NSData)
                continuation.resume(response)
            },
            onError = { error: NSError ->
                val exception =
                    KMStatusException(
                        KMStatus(KMCode.getCodeForValue(error.code.toInt()), error.description ?: "No description"),
                        null
                    )

                continuation.resumeWithException(exception)
            },
            onDone = {}
        )

        val call = GRPCCall2(
            requestOptions = channel.buildRequestOptions(path),
            responseHandler = handler,
            callOptions = channel.applyToCallOptions(callOptions)
        )

        call.start()
        call.writeData(data)

        call.finish()
    }
}

private sealed class StreamingResponse<T : KMMessage> {
    class Message<T : KMMessage>(val msg: T) : StreamingResponse<T>()
    class StatusException<T : KMMessage>(val exception: KMStatusException) : StreamingResponse<T>()
}

suspend fun <REQ : KMMessage, RES : KMMessage> serverSideStreamingCallImplementation(
    channel: KMChannel,
    callOptions: GRPCCallOptions,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES, NSData>
): Flow<RES> {
    val flow = MutableSharedFlow<StreamingResponse<RES>>()
    val isDone = MutableStateFlow(false)

    val scope = CoroutineScope(coroutineContext)

    val handler = CallHandler(
        onReceive = { data ->
            val msg = responseDeserializer.deserialize(data as NSData)
            scope.launch {
                flow.emit(StreamingResponse.Message(msg))
            }
        },
        onError = { error ->
            val exception = KMStatusException(
                KMStatus(KMCode.getCodeForValue(error.code.toInt()), error.description ?: "No description"),
                null
            )

            scope.launch {
                flow.emit(StreamingResponse.StatusException(exception))
            }
        },
        onDone = {
            isDone.value = true
        }
    )

    val call = GRPCCall2(channel.buildRequestOptions(path), handler, channel.applyToCallOptions(callOptions))

    call.start()
    call.writeData(request.serialize())
    call.finish()

    return isDone.takeWhile { !it }.transform {
        flow.collect { response ->
            when (response) {
                is StreamingResponse.Message -> emit(response.msg)
                is StreamingResponse.StatusException -> throw response.exception
            }
        }
    }.onCompletion {
        try {
            scope.cancel()
        } catch (_: IllegalStateException) {
        }
    }
}

private class CallHandler(
    private val onReceive: (data: Any) -> Unit,
    private val onError: (error: NSError) -> Unit,
    private val onDone: () -> Unit
) :
    NSObject(), GRPCResponseHandlerProtocol {

    override fun dispatchQueue(): dispatch_queue_t = null

    override fun didReceiveData(data: Any) = onReceive(data)

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        if (error != null) {
            onError(error)
        }
        onDone()
    }
}
