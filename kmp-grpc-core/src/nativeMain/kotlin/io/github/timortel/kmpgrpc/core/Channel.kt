package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.CallInterceptorChain
import io.github.timortel.kmpgrpc.core.internal.EmptyCallInterceptor
import io.github.timortel.kmpgrpc.native.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.cinterop.CPointer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext

actual class Channel private constructor(
    internal val name: String,
    internal val port: Int,
    private val usePlaintext: Boolean,
    /**
     * The interceptor associated with this channel, or null.
     */
    internal val interceptor: CallInterceptor,
    internal val keepAliveConfig: KeepAliveConfig
) : NativeJsChannel() {

    /*
    grpc.ready().await throws a Segfault when we do not execute all rpcs on the same thread.
    */
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    internal val context = newSingleThreadContext("native channel executor - $name:$port")

    internal val channel: CPointer<cnames.structs.RustChannel>?

    init {
        val host = (if (usePlaintext) "http://" else "https://") + "$name:$port"

        val (keepAliveTime, keepAliveTimeout, keepAliveWithoutCalls) = when (keepAliveConfig) {
            is KeepAliveConfig.Disabled -> {
                val defaults = KeepAliveConfig.Enabled()
                Triple(defaults.time, defaults.timeout, defaults.withoutCalls)
            }
            is KeepAliveConfig.Enabled -> Triple(keepAliveConfig.time, keepAliveConfig.timeout, keepAliveConfig.withoutCalls)
        }

        channel = channel_create(
            host,
            usePlaintext,
            keepAliveTime.inWholeNanoseconds.toULong(),
            keepAliveTimeout.inWholeNanoseconds.toULong(),
            keepAliveWithoutCalls
        ) ?: throw IllegalArgumentException("$host is not a valid uri.")
    }


    actual class Builder(private val name: String, private val port: Int) {

        private var usePlaintext = false

        private var interceptor: CallInterceptor = EmptyCallInterceptor

        private var keepAliveConfig: KeepAliveConfig = KeepAliveConfig.Disabled

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder = Builder(name, port)
        }

        actual fun usePlaintext(): Builder {
            usePlaintext = true
            return this
        }

        actual fun withInterceptors(vararg interceptors: CallInterceptor): Builder = apply {
            val newInterceptorsInterceptor =
                if (interceptors.size == 1) interceptors.first()
                else CallInterceptorChain(interceptors.toList())

            interceptor = when (val interceptor = interceptor) {
                is CallInterceptorChain -> interceptor + newInterceptorsInterceptor
                else -> CallInterceptorChain(listOf(interceptor) + interceptors.toList())
            }
        }

        actual fun withKeepAliveConfig(config: KeepAliveConfig): Builder = apply {
            keepAliveConfig = config
        }

        actual fun build(): Channel {
            return Channel(
                name,
                port,
                usePlaintext,
                interceptor,
                keepAliveConfig
            )
        }
    }

    override fun cleanupResources() {
        channel_free(channel)
        context.close()
    }

    companion object {
        init {
            init(ENABLE_TRACE_LOGGING)
        }
    }
}
