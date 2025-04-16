package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.message.KMMessage
import io.grpc.*
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener

/**
 * Implementation of the gRPC interceptor forwarding calls to [impl]
 */
class ClientInterceptorImpl(private val impl: CallInterceptor) : ClientInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val kmMethodDescriptor = KMMethodDescriptor(
            fullMethodName = method.fullMethodName,
            methodType = when (method.type) {
                MethodDescriptor.MethodType.UNARY -> KMMethodDescriptor.MethodType.UNARY
                MethodDescriptor.MethodType.CLIENT_STREAMING -> KMMethodDescriptor.MethodType.CLIENT_STREAMING
                MethodDescriptor.MethodType.SERVER_STREAMING -> KMMethodDescriptor.MethodType.SERVER_STREAMING
                MethodDescriptor.MethodType.BIDI_STREAMING, MethodDescriptor.MethodType.UNKNOWN, null -> KMMethodDescriptor.MethodType.BIDI_STREAMING
            }
        )

        return object : SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                val newMetadata = impl.onStart(
                    methodDescriptor = kmMethodDescriptor,
                    metadata = headers.kmMetadata
                )

                super.start(
                    object : SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onHeaders(headers: Metadata) {
                            super.onHeaders(impl.onReceiveHeaders(kmMethodDescriptor, headers.kmMetadata).jvmMetadata)
                        }

                        override fun onMessage(message: RespT) {
                            if (message is KMMessage) {
                                super.onMessage(impl.onReceiveMessage(kmMethodDescriptor, message))
                            } else {
                                super.onMessage(message)
                            }
                        }

                        override fun onClose(status: Status, trailers: Metadata) {
                            val (newStatus, newTrailers) = impl.onClose(
                                kmMethodDescriptor,
                                KMStatus(
                                    KMCode.getCodeForValue(status.code.value()),
                                    status.description.orEmpty()
                                ), trailers.kmMetadata
                            )

                            super.onClose(newStatus.jvmStatus, newTrailers.jvmMetadata)
                        }
                    },
                    newMetadata.jvmMetadata
                )
            }

            override fun sendMessage(message: ReqT) {
                if (message is KMMessage) {
                    super.sendMessage(impl.onSendMessage(kmMethodDescriptor, message) as ReqT)
                } else {
                    super.sendMessage(message)
                }
            }
        }
    }
}
