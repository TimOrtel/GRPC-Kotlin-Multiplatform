package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.Code
import io.github.timortel.kmpgrpc.core.JvmMetadata
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.core.Status
import io.github.timortel.kmpgrpc.core.jvmMetadata
import io.github.timortel.kmpgrpc.core.jvmStatus
import io.github.timortel.kmpgrpc.core.kmMetadata
import io.github.timortel.kmpgrpc.core.message.Message
import io.grpc.CallOptions
import io.grpc.MethodDescriptor as JvmMethodDescriptor
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Status as JvmStatus

/**
 * Implementation of the gRPC interceptor forwarding calls to [impl]
 */
internal class ClientInterceptorImpl(private val impl: CallInterceptor) : ClientInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: JvmMethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val methodDescriptor = MethodDescriptor(
            fullMethodName = method.fullMethodName,
            methodType = when (method.type) {
                JvmMethodDescriptor.MethodType.UNARY -> MethodDescriptor.MethodType.UNARY
                JvmMethodDescriptor.MethodType.CLIENT_STREAMING -> MethodDescriptor.MethodType.CLIENT_STREAMING
                JvmMethodDescriptor.MethodType.SERVER_STREAMING -> MethodDescriptor.MethodType.SERVER_STREAMING
                JvmMethodDescriptor.MethodType.BIDI_STREAMING, JvmMethodDescriptor.MethodType.UNKNOWN, null -> MethodDescriptor.MethodType.BIDI_STREAMING
            }
        )

        return object : SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: JvmMetadata) {
                val newMetadata = impl.onStart(
                    methodDescriptor = methodDescriptor,
                    metadata = headers.kmMetadata
                )

                super.start(
                    object : SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onHeaders(headers: JvmMetadata) {
                            super.onHeaders(impl.onReceiveHeaders(methodDescriptor, headers.kmMetadata).jvmMetadata)
                        }

                        override fun onMessage(message: RespT) {
                            if (message is Message) {
                                super.onMessage(impl.onReceiveMessage(methodDescriptor, message))
                            } else {
                                super.onMessage(message)
                            }
                        }

                        override fun onClose(status: JvmStatus, trailers: JvmMetadata) {
                            val (newStatus, newTrailers) = impl.onClose(
                                methodDescriptor = methodDescriptor,
                                status = Status(
                                    code = Code.getCodeForValue(status.code.value()),
                                    statusMessage = status.description.orEmpty()
                                ),
                                metadata = trailers.kmMetadata
                            )

                            super.onClose(newStatus.jvmStatus, newTrailers.jvmMetadata)
                        }
                    },
                    newMetadata.jvmMetadata
                )
            }

            override fun sendMessage(message: ReqT) {
                if (message is Message) {
                    super.sendMessage(impl.onSendMessage(methodDescriptor, message) as ReqT)
                } else {
                    super.sendMessage(message)
                }
            }
        }
    }
}
