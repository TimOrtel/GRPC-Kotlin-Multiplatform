package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.external.ClientReadableStream
import io.github.timortel.kmpgrpc.core.external.Request
import io.github.timortel.kmpgrpc.core.external.StreamInterceptor
import kotlin.js.json

internal class StreamCallInterceptorWrapper(override val impl: CallInterceptor) : StreamInterceptor, InterceptorBase {

    override fun intercept(request: Request, invoker: (dynamic) -> ClientReadableStream): ClientReadableStream {
        val methodDescriptor = getMethodDescriptor(request)

        return Interceptor(invoker(interceptRequest(request)), methodDescriptor)
    }

    private inner class Interceptor(
        private val streamImpl: ClientReadableStream,
        private val methodDescriptor: KMMethodDescriptor,
    ) : ClientReadableStream {

        override fun on(eventType: String, callback: (dynamic) -> Unit): ClientReadableStream {
            when (eventType) {
                "data" -> streamImpl.on("data") { response ->
                    if (response is Message) {
                        callback(impl.onReceiveMessage(methodDescriptor, response))
                    } else {
                        callback(response)
                    }
                }

                "status" -> streamImpl.on("status") { response ->
                    val code = response.code.unsafeCast<Int>()
                    val details = response.details.unsafeCast<String?>()
                    val metadata = response.metadata

                    val (newStatus, newTrailing) = impl.onClose(
                        methodDescriptor = methodDescriptor,
                        status = Status(Code.getCodeForValue(code), details.orEmpty()),
                        metadata = getMetadataFromJs(metadata)
                    )

                    callback(
                        json(
                            "code" to newStatus.code.value,
                            "details" to newStatus.statusMessage,
                            "metadata" to newTrailing.jsMetadata
                        )
                    )
                }
                else -> streamImpl.on(eventType, callback)
            }

            return this
        }

        override fun cancel(): ClientReadableStream {
            streamImpl.cancel()
            return this
        }
    }
}
