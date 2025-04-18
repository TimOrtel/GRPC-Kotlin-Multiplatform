package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.external.MethodDescriptor as JsMethodDescriptor
import io.github.timortel.kmpgrpc.core.external.Request
import io.github.timortel.kmpgrpc.core.external.UnaryResponse
import io.github.timortel.kmpgrpc.core.jsMetadata
import io.github.timortel.kmpgrpc.core.kmMethodType
import io.github.timortel.kmpgrpc.core.message.Message

internal interface InterceptorBase {

    val impl: CallInterceptor

    /**
     * Forwards the request to [impl]
     */
    fun interceptRequest(request: Request): dynamic {
        val methodDescriptor = getMethodDescriptor(request)

        val newMetadata = impl.onStart(
            methodDescriptor = methodDescriptor,
            metadata = getMetadata(request)
        )

        val requestMessage = request.getRequestMessage()
        val newRequestMessage = if (requestMessage is Message) {
            impl.onSendMessage(methodDescriptor, requestMessage)
        } else requestMessage

        return RequestImpl(
            requestMessage = newRequestMessage,
            methodDescriptor = request.getMethodDescriptor(),
            metadata = newMetadata.jsMetadata,
            callOptions = request.getCallOptions()
        )
    }

    fun <RESP> interceptMessage(response: UnaryResponse<RESP>): dynamic {
        val responseMessage = response.getResponseMessage()

        return if (responseMessage is Message) {
            impl.onReceiveMessage(getMethodDescriptor(response), responseMessage)
        } else responseMessage
    }

    fun getMethodDescriptor(request: Request): MethodDescriptor {
        return getMethodDescriptor(request.getMethodDescriptor())
    }

    fun <RESP> getMethodDescriptor(response: UnaryResponse<RESP>): MethodDescriptor {
        return getMethodDescriptor(response.getMethodDescriptor())
    }

    private fun getMethodDescriptor(methodDescriptor: JsMethodDescriptor): MethodDescriptor {
        return MethodDescriptor(
            fullMethodName = methodDescriptor.getName(),
            methodType = methodDescriptor.methodType.kmMethodType
        )
    }

    fun <RESP> getKmMetadata(response: UnaryResponse<RESP>): Metadata {
        return getMetadataFromJs(response.getMetadata())
    }

    private fun getMetadata(request: Request): Metadata {
        return getMetadataFromJs(request.getMetadata())
    }

    fun getMetadataFromJs(jsObj: dynamic): Metadata {
        val keys = js("Object.keys(jsObj)").unsafeCast<Array<dynamic>>()

        return Metadata.of(
            buildMap {
                keys.forEach { key ->
                    put(key.toString(), jsObj.get(key).toString())
                }
            }.toMutableMap()
        )
    }
}
