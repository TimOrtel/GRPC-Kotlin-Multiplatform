package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.CallInterceptorChain
import io.github.timortel.kmpgrpc.core.internal.EmptyCallInterceptor
import io.github.timortel.kmpgrpc.native.*
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
) : NativeJsChannel() {

    /*
    grpc.ready().await throws a Segfault when we do not execute all rpcs on the same thread.
     */
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    internal val context = newSingleThreadContext("native channel executor - $name:$port")

    internal val channel: CPointer<cnames.structs.RustChannel>?

    init {
        val host = (if (usePlaintext) "http://" else "https://") + "$name:$port"

        channel = channel_create(host, usePlaintext)
        if (channel == null) {
            throw IllegalArgumentException("$host is not a valid uri.")
        }
    }

    actual class Builder(private val name: String, private val port: Int) {

        private var usePlaintext = false

        private var interceptor: CallInterceptor = EmptyCallInterceptor

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

        actual fun build(): Channel = Channel(name, port, usePlaintext, interceptor)
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
