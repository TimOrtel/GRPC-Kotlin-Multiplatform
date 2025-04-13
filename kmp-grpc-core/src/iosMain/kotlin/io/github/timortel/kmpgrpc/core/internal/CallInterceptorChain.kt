package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.KMStatus
import io.github.timortel.kmpgrpc.core.message.KMMessage

/**
 * Implementation of a [CallInterceptor] that calls all given [callInterceptors].
 * The first element in [callInterceptors] is the outermost interceptor.
 */
internal class CallInterceptorChain(
    private val callInterceptors: List<CallInterceptor>
) : CallInterceptor {

    override fun onStart(methodDescriptor: KMMethodDescriptor, metadata: KMMetadata): KMMetadata {
        return callInterceptors.foldRight(metadata) { interceptor, currentMetadata ->
            interceptor.onStart(
                methodDescriptor,
                currentMetadata
            )
        }
    }

    override fun <T : KMMessage> onSendMessage(methodDescriptor: KMMethodDescriptor, message: T): T {
        return callInterceptors.foldRight(message) { interceptor, currentMessage ->
            interceptor.onSendMessage(
                methodDescriptor,
                currentMessage
            )
        }
    }

    override fun onReceiveHeaders(methodDescriptor: KMMethodDescriptor, metadata: KMMetadata): KMMetadata {
        return callInterceptors.foldRight(metadata) { interceptor, currentMetadata ->
            interceptor.onReceiveHeaders(
                methodDescriptor,
                currentMetadata
            )
        }
    }

    override fun <T : KMMessage> onReceiveMessage(methodDescriptor: KMMethodDescriptor, message: T): T {
        return callInterceptors.foldRight(message) { interceptor, currentMessage ->
            interceptor.onReceiveMessage(
                methodDescriptor,
                currentMessage
            )
        }
    }

    override fun onClose(
        methodDescriptor: KMMethodDescriptor,
        status: KMStatus,
        metadata: KMMetadata
    ): Pair<KMStatus, KMMetadata> {
        return callInterceptors.foldRight(status to metadata) { interceptor, (currentStatus, currentMetadata) ->
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
