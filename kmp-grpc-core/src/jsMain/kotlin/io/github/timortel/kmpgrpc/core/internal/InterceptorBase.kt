package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.message.KMMessage
import io.github.timortel.kmpgrpc.core.rpc.MethodDescriptor
import io.github.timortel.kmpgrpc.core.rpc.Request
import io.github.timortel.kmpgrpc.core.rpc.RequestInternal
import io.github.timortel.kmpgrpc.core.rpc.UnaryResponse

internal interface InterceptorBase {

    val impl: CallInterceptor

    /**
     * Forwards the request to [impl]
     */
    fun interceptRequest(request: Request): dynamic {
        val methodDescriptor = getMethodDescriptor(request)

        val newMetadata = impl.onStart(
            methodDescriptor = methodDescriptor,
            metadata = KMMetadata(request.getMetadata())
        )

        val requestMessage = request.getRequestMessage()
        val newRequestMessage = if (requestMessage is KMMessage) {
            impl.onSendMessage(methodDescriptor, requestMessage)
        } else requestMessage

        return RequestInternal(
            requestMessage = newRequestMessage,
            methodDescriptor = request.getMethodDescriptor(),
            metadata = newMetadata.metadataMap,
            callOptions = request.getCallOptions()
        )
    }

    fun interceptMessage(response: UnaryResponse): dynamic {
        val responseMessage = response.getResponseMessage()
        response.getMethodDescriptor()

        return if (responseMessage is KMMessage) {
            impl.onReceiveMessage(getMethodDescriptor(response), responseMessage)
        } else responseMessage
    }

    fun getMethodDescriptor(request: Request): KMMethodDescriptor {
        return getMethodDescriptor(request.getMethodDescriptor())
    }

    fun getMethodDescriptor(response: UnaryResponse): KMMethodDescriptor {
        return getMethodDescriptor(response.getMethodDescriptor())
    }

    private fun getMethodDescriptor(methodDescriptor: MethodDescriptor): KMMethodDescriptor {
        return KMMethodDescriptor(
            fullMethodName = methodDescriptor.getName(),
            methodType = methodDescriptor.getMethodType().kmMethodType
        )
    }
}
