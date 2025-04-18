package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.Code
import io.github.timortel.kmpgrpc.core.JvmMetadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.jvmMetadata
import io.github.timortel.kmpgrpc.core.jvmStatus
import io.github.timortel.kmpgrpc.core.kmMetadata
import io.github.timortel.kmpgrpc.core.message.Message
import io.grpc.*
import io.grpc.Channel
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener

/**
 * Implementation of the gRPC interceptor forwarding calls to [impl]
 */
internal class ClientInterceptorImpl(private val impl: CallInterceptor) : ClientInterceptor {

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
            override fun start(responseListener: Listener<RespT>, headers: JvmMetadata) {
                val newMetadata = impl.onStart(
                    methodDescriptor = kmMethodDescriptor,
                    metadata = headers.kmMetadata
                )

                super.start(
                    object : SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onHeaders(headers: JvmMetadata) {
                            super.onHeaders(impl.onReceiveHeaders(kmMethodDescriptor, headers.kmMetadata).jvmMetadata)
                        }

                        override fun onMessage(message: RespT) {
                            if (message is Message) {
                                super.onMessage(impl.onReceiveMessage(kmMethodDescriptor, message))
                            } else {
                                super.onMessage(message)
                            }
                        }

                        override fun onClose(status: Status, trailers: JvmMetadata) {
                            val (newStatus, newTrailers) = impl.onClose(
                                methodDescriptor = kmMethodDescriptor,
                                status = io.github.timortel.kmpgrpc.core.Status(
                                    Code.getCodeForValue(status.code.value()),
                                    status.description.orEmpty()
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
                    super.sendMessage(impl.onSendMessage(kmMethodDescriptor, message) as ReqT)
                } else {
                    super.sendMessage(message)
                }
            }
        }
    }
}
