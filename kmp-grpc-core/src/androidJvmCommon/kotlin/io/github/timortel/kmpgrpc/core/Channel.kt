package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.config.KeepAliveConfig
import io.github.timortel.kmpgrpc.core.internal.ClientInterceptorImpl
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.resume

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
                    impl.keepAliveTime(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
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

        awaitTermination()
    }

    actual suspend fun shutdownNow() {
        channel.shutdownNow()

        awaitTermination()
    }

    private suspend fun awaitTermination() {
        suspendCancellableCoroutine { continuation ->
            val t = thread {
                try {
                    channel.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)

                    if (continuation.isActive) continuation.resume(Unit)
                } catch (e: InterruptedException) {
                    if (continuation.isActive) continuation.cancel(e)
                }
            }

            continuation.invokeOnCancellation {
                t.interrupt()
            }
        }
    }
}
