package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.ClientInterceptorImpl
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlin.time.Duration
import java.util.concurrent.TimeUnit

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

        actual fun withKeepAliveConfig(config: KeepAliveConfig): Builder = apply {
            when (config) {
                is KeepAliveConfig.Disabled -> {
                    // KeepAlive is disabled by default in gRPC Java
                }
                is KeepAliveConfig.Enabled -> {
                    impl.keepAliveTime(config.time.inWholeNanoseconds, TimeUnit.NANOSECONDS)
                    impl.keepAliveTimeout(config.timeout.inWholeNanoseconds, TimeUnit.NANOSECONDS)
                    impl.keepAliveWithoutCalls(config.withoutCalls)
                }
            }
        }

        actual fun build(): Channel = Channel(impl.build())
    }

    actual val isTerminated: Boolean
        get() = channel.isTerminated

    actual suspend fun shutdown() {
        channel.shutdown()
    }

    actual suspend fun shutdownNow() {
        channel.shutdownNow()
    }
}
