package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.core.Request
import io.github.timortel.kmpgrpc.core.UnaryResponse
import io.github.timortel.kmpgrpc.core.jsMetadata
import io.github.timortel.kmpgrpc.core.message.KMMessage

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
        val newRequestMessage = if (requestMessage is KMMessage) {
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

        return if (responseMessage is KMMessage) {
            impl.onReceiveMessage(getMethodDescriptor(response), responseMessage)
        } else responseMessage
    }

    fun getMethodDescriptor(request: Request): KMMethodDescriptor {
        return getMethodDescriptor(request.getMethodDescriptor())
    }

    fun <RESP> getMethodDescriptor(response: UnaryResponse<RESP>): KMMethodDescriptor {
        return getMethodDescriptor(response.getMethodDescriptor())
    }

    private fun getMethodDescriptor(methodDescriptor: MethodDescriptor): KMMethodDescriptor {
        return KMMethodDescriptor(
            fullMethodName = methodDescriptor.getName(),
            methodType = methodDescriptor.methodType.kmMethodType
        )
    }

    fun <RESP> getKmMetadata(response: UnaryResponse<RESP>): KMMetadata {
        return getMetadataFromJs(response.getMetadata())
    }

    private fun getMetadata(request: Request): KMMetadata {
        return getMetadataFromJs(request.getMetadata())
    }

    fun getMetadataFromJs(jsObj: dynamic): KMMetadata {
        val keys = js("Object.keys(jsObj)").unsafeCast<Array<dynamic>>()

        return KMMetadata(
            buildMap {
                keys.forEach { key ->
                    put(key.toString(), jsObj.get(key).toString())
                }
            }.toMutableMap()
        )
    }
}
