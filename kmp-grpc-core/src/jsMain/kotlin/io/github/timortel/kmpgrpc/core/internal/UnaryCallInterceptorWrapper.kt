package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.rpc.*
import kotlin.js.Promise

internal class UnaryCallInterceptorWrapper(override val impl: CallInterceptor) : UnaryInterceptor, InterceptorBase {

    override fun intercept(request: Request, invoker: (dynamic) -> Promise<UnaryResponse>) {
        val newRequest = interceptRequest(request)

        invoker(newRequest).then { response ->
            val newResponseMessage = interceptMessage(response)

            val newHeaders = impl.onReceiveHeaders(
                methodDescriptor = getMethodDescriptor(response),
                metadata = KMMetadata(response.getMetadata())
            )

            val status = KMStatus(
                code = KMCode.getCodeForValue(response.getStatus().code.toInt()),
                statusMessage = response.getStatus().details
            )

            val (newStatus, newTrailers) = impl.onClose(
                methodDescriptor = getMethodDescriptor(response),
                status = status,
                metadata = newHeaders
            )

            UnaryResponseInternal(
                responseMessage = newResponseMessage,
                methodDescriptor = request.getMethodDescriptor(),
                metadata = newTrailers.metadataMap,
                status = newStatus.toJsStatus(newTrailers)
            )
        }
    }
}
