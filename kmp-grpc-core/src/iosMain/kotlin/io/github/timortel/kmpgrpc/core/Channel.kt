package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.CallInterceptorChain
import io.github.timortel.kmpgrpc.core.internal.createNativeChannelWithInterceptors
import io.github.timortel.kmpgrpc.native.*
import kotlinx.cinterop.CPointer

actual class Channel private constructor(
    internal val name: String,
    internal val port: Int,
    private val usePlaintext: Boolean,
    /**
     * The interceptor associated with this channel, or null.
     */
    internal val interceptor: CallInterceptor? = null
) : IosJsChannel() {

    internal val channel: CPointer<cnames.structs.grpc_channel>?

    init {
        val host = "$name:$port"

        channel = if (interceptor != null) {
            createNativeChannelWithInterceptors(host, interceptor)
        } else {
            create_insecure_channel(
                host = host
            )
        }
    }

    actual class Builder(private val name: String, private val port: Int) {

        private var usePlaintext = false

        private var interceptor: CallInterceptor? = null

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
                null -> newInterceptorsInterceptor
                is CallInterceptorChain -> interceptor + newInterceptorsInterceptor
                else -> CallInterceptorChain(listOf(interceptor) + interceptors.toList())
            }
        }

        actual fun build(): Channel = Channel(name, port, usePlaintext, interceptor)
    }

    actual override fun shutdown() {
        super.shutdown()
    }
}
