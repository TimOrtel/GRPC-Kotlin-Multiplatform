package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.external.Request
import io.github.timortel.kmpgrpc.core.external.UnaryInterceptor
import io.github.timortel.kmpgrpc.core.external.UnaryResponse
import kotlin.js.Promise

internal class UnaryCallInterceptorWrapper(override val impl: CallInterceptor) : UnaryInterceptor, InterceptorBase {

    override fun <RESP> intercept(
        request: Request,
        invoker: (dynamic) -> Promise<UnaryResponse<RESP>>
    ): Promise<UnaryResponse<RESP>> {
        val newRequest = interceptRequest(request)

        return invoker(newRequest).asDynamic().then { response: UnaryResponse<RESP> ->
            val newHeaders = impl.onReceiveHeaders(
                methodDescriptor = getMethodDescriptor(response),
                metadata = getKmMetadata(response)
            )

            val newResponseMessage = interceptMessage(response)

            val status = Status(
                code = Code.getCodeForValue(response.getStatus().code.toInt()),
                statusMessage = response.getStatus().details
            )

            val (newStatus, newTrailers) = impl.onClose(
                methodDescriptor = getMethodDescriptor(response),
                status = status,
                metadata = newHeaders
            )

            if (newStatus.code != Code.OK) {
                Promise.reject(StatusException(newStatus, null))
            } else {
                Promise.resolve(
                    UnaryResponseImpl(
                        resp = newResponseMessage,
                        methodDescriptor = request.getMethodDescriptor(),
                        metadata = newTrailers.jsMetadata,
                        status = newStatus.toJsStatus(newTrailers)
                    )
                )
            }
        }
    }
}
