package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.rpc.*

internal class StreamCallInterceptorWrapper(override val impl: CallInterceptor) : StreamInterceptor, InterceptorBase {

    override fun intercept(request: Request, invoker: (dynamic) -> ClientReadableStream): ClientReadableStream {
        return Interceptor(invoker(interceptRequest(request)))
    }

    private inner class Interceptor(private val streamImpl: ClientReadableStream) : ClientReadableStream {
        override fun on(eventType: String, callback: (dynamic) -> Unit): ClientReadableStream {
            when (eventType) {
                "data" -> streamImpl.on("data") { response ->
                    val newMessage = interceptMessage(response)

                    callback(response.copy(responseMessage = newMessage))
                }

                "metadata" -> streamImpl.on("metadata") { response ->
                    val newMetadata =
                        impl.onReceiveHeaders(getMethodDescriptor(response), KMMetadata(response.getMetadata()))

                    callback(response.copy(metadata = newMetadata.metadataMap))
                }

                "status" -> streamImpl.on("status") { response ->
                    val (newStatus, newTrailing) = impl.onClose(
                        methodDescriptor = getMethodDescriptor(response),
                        status = response.getStatus().toKmStatus,
                        metadata = KMMetadata(response.getStatus().metadata.orEmpty())
                    )

                    callback(
                        response.copy(
                            status = Status(
                                code = newStatus.code.value,
                                details = newStatus.statusMessage,
                                metadata = newTrailing.metadataMap
                            )
                        )
                    )
                }
            }

            return this
        }

        override fun cancel(): ClientReadableStream {
            streamImpl.cancel()
            return this
        }

        private fun UnaryResponse.copy(
            responseMessage: dynamic = this.getResponseMessage(),
            methodDescriptor: MethodDescriptor = this.getMethodDescriptor(),
            metadata: Map<String, String> = this.getMetadata(),
            status: Status = this.getStatus()
        ) = UnaryResponseInternal(
            responseMessage = responseMessage,
            methodDescriptor = methodDescriptor,
            metadata = metadata,
            status = status
        )
    }
}
