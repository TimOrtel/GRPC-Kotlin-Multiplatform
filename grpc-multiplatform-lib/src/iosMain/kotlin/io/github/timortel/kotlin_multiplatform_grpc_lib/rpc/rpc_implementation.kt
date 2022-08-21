package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import cocoapods.GRPCClient.GRPCCall2
import cocoapods.GRPCClient.GRPCResponseHandlerProtocol
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMCode
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatus
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatusException
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
import kotlinx.cinterop.rawValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Security.errSecInvalidReason
import platform.darwin.*
import platform.posix.QOS_CLASS_DEFAULT
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.native.concurrent.freeze

@Throws(KMStatusException::class, CancellationException::class)
suspend fun <REQ : KMMessage, RES : KMMessage> unaryCallImplementation(
    channel: KMChannel,
    path: String,
    request: REQ,
    responseDeserializer: MessageDeserializer<RES>
): RES {
    val data = request.serialize()
    println("Starting")

    return suspendCoroutine { continuation ->
        val handler = UnaryCallHandler(
            onReceive = { data: Any ->
                println("Received")
                val response = responseDeserializer.deserialize(data as NSData)
                continuation.resume(response)
            },
            onError = { error: NSError ->
                println("Error")
                val exception =
                    KMStatusException(
                        KMStatus(KMCode.getCodeForValue(error.code.toInt()), error.description ?: "No description"),
                        null
                    )

                continuation.resumeWithException(exception)
            }
        )

        val call = GRPCCall2(
            requestOptions = channel.buildRequestOptions(path),
            responseHandler = handler,
            callOptions = channel.callOptions
        )


        call.start()
        call.writeData(data)

        call.finish()
    }
}

private class UnaryCallHandler(
    private val onReceive: (data: Any) -> Unit,
    private val onError: (error: NSError) -> Unit
) :
    NSObject(), GRPCResponseHandlerProtocol {

    override fun dispatchQueue(): dispatch_queue_t = null

    override fun didReceiveData(data: Any) = onReceive(data)

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        println("Done")
        if (error != null) {
            onError(error)
        }
    }
}