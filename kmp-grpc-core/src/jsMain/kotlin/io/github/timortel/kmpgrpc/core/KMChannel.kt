package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.external.ClientOptions
import io.github.timortel.kmpgrpc.core.external.StreamInterceptor
import io.github.timortel.kmpgrpc.core.external.UnaryInterceptor
import io.github.timortel.kmpgrpc.core.internal.StreamCallInterceptorWrapper
import io.github.timortel.kmpgrpc.core.internal.UnaryCallInterceptorWrapper

actual class KMChannel private constructor(
    private val name: String,
    private val port: Int,
    private val usePlainText: Boolean,
    val clientOptions: ClientOptions
) {
    @Suppress("HttpUrlsUsage")
    val connectionString = (if (usePlainText) "http://" else "https://") + "$name:$port"

    actual data class Builder(val name: String, val port: Int) {

        private var usePlainText: Boolean = false

        private val interceptors = mutableListOf<CallInterceptor>()

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder = Builder(name, port)
        }

        actual fun usePlaintext(): Builder {
            usePlainText = true
            return this
        }

        actual fun withInterceptors(vararg interceptors: CallInterceptor): Builder = apply {
            this.interceptors += interceptors.toList()
        }

        actual fun build(): KMChannel {
            val unaryInterceptors =
                interceptors.map { UnaryCallInterceptorWrapper(it) }.toTypedArray<UnaryInterceptor>()
            val streamInterceptors =
                interceptors.map { StreamCallInterceptorWrapper(it) }.toTypedArray<StreamInterceptor>()

            val clientOptions: ClientOptions = js("{}")
                .unsafeCast<ClientOptions>()
                .apply {
                    format = "text"
                    this.unaryInterceptors = unaryInterceptors
                    this.streamInterceptors = streamInterceptors
                }

            return KMChannel(
                name = name,
                port = port,
                usePlainText = usePlainText,
                clientOptions = clientOptions
            )
        }
    }
}
