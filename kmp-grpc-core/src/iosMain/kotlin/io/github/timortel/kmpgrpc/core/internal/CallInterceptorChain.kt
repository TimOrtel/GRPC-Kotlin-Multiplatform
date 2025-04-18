package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.KMStatus
import io.github.timortel.kmpgrpc.core.message.Message

/**
 * Implementation of a [CallInterceptor] that calls all given [callInterceptors].
 * Runs from right to left on sending events, and from left to right on receiving events to match behavior on JVM and JS.
 */
internal class CallInterceptorChain(
    private val callInterceptors: List<CallInterceptor>
) : CallInterceptor {

    override fun onStart(methodDescriptor: KMMethodDescriptor, metadata: Metadata): Metadata {
        return callInterceptors.foldRight(metadata) { interceptor, currentMetadata ->
            interceptor.onStart(
                methodDescriptor,
                currentMetadata
            )
        }
    }

    override fun <T : Message> onSendMessage(methodDescriptor: KMMethodDescriptor, message: T): T {
        return callInterceptors.foldRight(message) { interceptor, currentMessage ->
            interceptor.onSendMessage(
                methodDescriptor,
                currentMessage
            )
        }
    }

    override fun onReceiveHeaders(methodDescriptor: KMMethodDescriptor, metadata: Metadata): Metadata {
        return callInterceptors.fold(metadata) { currentMetadata, interceptor ->
            interceptor.onReceiveHeaders(
                methodDescriptor,
                currentMetadata
            )
        }
    }

    override fun <T : Message> onReceiveMessage(methodDescriptor: KMMethodDescriptor, message: T): T {
        return callInterceptors.fold(message) { currentMessage, interceptor ->
            interceptor.onReceiveMessage(
                methodDescriptor,
                currentMessage
            )
        }
    }

    override fun onClose(
        methodDescriptor: KMMethodDescriptor,
        status: KMStatus,
        metadata: Metadata
    ): Pair<KMStatus, Metadata> {
        return callInterceptors.fold(status to metadata) { (currentStatus, currentMetadata), interceptor ->
            interceptor.onClose(
                methodDescriptor = methodDescriptor,
                status = currentStatus,
                metadata = currentMetadata
            )
        }
    }

    operator fun plus(other: CallInterceptor): CallInterceptorChain {
        return if (other is CallInterceptorChain) {
            CallInterceptorChain(callInterceptors + other.callInterceptors)
        } else CallInterceptorChain(callInterceptors + other)
    }
}
