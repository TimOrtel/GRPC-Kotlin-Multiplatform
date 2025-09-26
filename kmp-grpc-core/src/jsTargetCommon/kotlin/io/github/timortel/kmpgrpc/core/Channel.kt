package io.github.timortel.kmpgrpc.core

import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlin.time.Duration

actual class Channel private constructor(
    private val name: String,
    private val port: Int,
    private val usePlainText: Boolean,
    internal val interceptors: List<CallInterceptor>,
) : NativeJsChannel() {

    @Suppress("HttpUrlsUsage")
    val connectionString = (if (usePlainText) "http://" else "https://") + "$name:$port"

    val client = HttpClient {
        install(HttpTimeout) {}
    }

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

        actual fun withKeepAliveConfig(config: KeepAliveConfig): Builder = apply {
            // JavaScript/WASM: KeepAlive not supported - delegate to underlying gRPC libraries
            // No-op implementation
        }

        actual fun build(): Channel {
            return Channel(
                name = name,
                port = port,
                usePlainText = usePlainText,
                interceptors = interceptors
            )
        }
    }

    override fun cleanupResources() {
        client.close()
    }
}
