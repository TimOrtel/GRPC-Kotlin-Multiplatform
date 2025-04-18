package io.github.timortel.kmpgrpc.core

import cocoapods.GRPCClient.*
import io.github.timortel.kmpgrpc.core.internal.CallInterceptorChain

/**
 * On ios the channel equivalent are the [GRPCCallOptions].
 */
actual class Channel private constructor(
    private val name: String,
    private val port: Int,
    private val usePlaintext: Boolean,
    /**
     * The interceptor associated with this channel, or null.
     */
    val interceptor: CallInterceptor? = null
) : IosJsChannel() {

    fun buildRequestOptions(path: String) = GRPCRequestOptions("$name:$port", path, safety = GRPCCallSafetyDefault)

    /**
     * Applies configuration of the channel to the given call options.
     * If any mutations are performed, a new copy of call options is returned. The original call options
     * are left unmodified.
     */
    fun applyToCallOptions(callOptions: GRPCCallOptions): GRPCCallOptions {
        return if (usePlaintext) {
            val newCallOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
            newCallOptions.setTransport(GRPCDefaultTransportImplList_.core_insecure)
            newCallOptions
        } else callOptions
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
}
