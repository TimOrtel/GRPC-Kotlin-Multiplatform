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
    internal val keepAliveTime: Duration,
    internal val keepAliveTimeout: Duration,
    internal val keepAliveWithoutCalls: Boolean
) : NativeJsChannel() {

    /*
    grpc.ready().await throws a Segfault when we do not execute all rpcs on the same thread.
    */
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    internal val context = newSingleThreadContext("native channel executor - $name:$port")

    internal val channel: CPointer<cnames.structs.RustChannel>?

    init {
        val host = (if (usePlaintext) "http://" else "https://") + "$name:$port"

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

        private var keepAliveTime: Duration = Duration.INFINITE
        private var keepAliveTimeout: Duration = 20.seconds
        private var keepAliveWithoutCalls: Boolean = false

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

        actual fun keepAliveTime(duration: Duration): Builder = apply {
            keepAliveTime = duration
        }

        actual fun keepAliveTimeout(duration: Duration): Builder = apply {
            keepAliveTimeout = duration
        }

        actual fun keepAliveWithoutCalls(keepAliveWithoutCalls: Boolean): Builder = apply {
            this.keepAliveWithoutCalls = keepAliveWithoutCalls
        }

        actual fun build(): Channel {
            return Channel(
                name,
                port,
                usePlaintext,
                interceptor,
                keepAliveTime,
                keepAliveTimeout,
                keepAliveWithoutCalls
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
