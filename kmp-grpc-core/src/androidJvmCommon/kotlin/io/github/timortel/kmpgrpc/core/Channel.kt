package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.ClientInterceptorImpl
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

/**
 * The Jvm [Channel] wraps the grpc [ManagedChannel] and delegates its operations to the wrapped native channel.
 */
actual class Channel private constructor(val channel: ManagedChannel) {
    actual class Builder(private val impl: ManagedChannelBuilder<*>) {

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder {
                return Builder(ManagedChannelBuilder.forAddress(name, port))
            }
        }

        actual fun withInterceptors(vararg interceptors: CallInterceptor): Builder = apply {
            val grpcInterceptors = interceptors.map { ClientInterceptorImpl(it) }.toTypedArray()

            impl.intercept(*grpcInterceptors)
        }

        actual fun usePlaintext(): Builder = apply {
            impl.usePlaintext()
        }

        actual fun build(): Channel = Channel(impl.build())
    }

    actual fun shutdown() {
        channel.shutdown()
    }

    actual fun shutdownNow() {
        channel.shutdownNow()
    }
}
