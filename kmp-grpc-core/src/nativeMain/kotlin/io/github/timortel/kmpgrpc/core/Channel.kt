package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.config.KeepAliveConfig
import io.github.timortel.kmpgrpc.core.internal.CallInterceptorChain
import io.github.timortel.kmpgrpc.core.internal.EmptyCallInterceptor
import io.github.timortel.kmpgrpc.native.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext

actual class Channel private constructor(
    internal val name: String,
    internal val port: Int,
    private val usePlaintext: Boolean,
    private val certificates: List<Certificate>?,
    private val trustOnlyProvidedCertificates: Boolean,
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

        val enableKeepalive = when (keepAliveConfig) {
            is KeepAliveConfig.Enabled -> true
            KeepAliveConfig.Disabled -> false
        }

        val (keepAliveTime, keepAliveTimeout, keepAliveWithoutCalls) = when (keepAliveConfig) {
            is KeepAliveConfig.Disabled -> Triple(0.seconds, 0.seconds, false)
            is KeepAliveConfig.Enabled -> Triple(keepAliveConfig.time, keepAliveConfig.timeout, keepAliveConfig.withoutCalls)
        }

        val builder = channel_builder_create(
            host = host,
            use_plaintext = usePlaintext,
            enable_keepalive = enableKeepalive,
            keepalive_time_nanos = keepAliveTime.inWholeNanoseconds.toULong(),
            keepalive_timeout_nanos = keepAliveTimeout.inWholeNanoseconds.toULong(),
            keepalive_without_calls = keepAliveWithoutCalls
        ) ?: throw IllegalArgumentException("$host is not a valid uri.")

        if (!usePlaintext) {
            val tlsConfig = tls_config_create()
            if (certificates != null) {
                installCertificates(certificates, tlsConfig)
            }

            if (!trustOnlyProvidedCertificates) {
                tls_config_use_webpki_roots(tlsConfig)
            }

            channel_builder_use_tls_config(builder, tlsConfig)
        }

        channel = channel_builder_build(builder)

        if (channel == null) {
            throw IllegalArgumentException("$host is not a valid uri.")
        }
    }

    actual class Builder(private val name: String, private val port: Int) {

        private var usePlaintext = false
        private var certificates: List<Certificate>? = null
        private var trustOnlyProvidedCertificates = false

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
            certificates = null
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

        actual fun withTrustedCertificates(vararg certificates: Certificate): Builder {
            return withTrustedCertificates(certificates.toList())
        }

        actual fun withTrustedCertificates(certificates: List<Certificate>): Builder {
            usePlaintext = false
            this.certificates = certificates
            return this
        }

        actual fun trustOnlyProvidedCertificates(): Builder {
            trustOnlyProvidedCertificates = true
            return this
        }

        actual fun build(): Channel {
            return Channel(
                name = name,
                port = port,
                usePlaintext = usePlaintext,
                certificates = certificates,
                trustOnlyProvidedCertificates = trustOnlyProvidedCertificates,
                interceptor = interceptor,
                keepAliveConfig = keepAliveConfig
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

private fun installCertificates(certificates: List<Certificate>, tlsConfig: CPointer<cnames.structs.RustTlsConfigBuilder>?) {
    certificates
        .forEach { certificate ->
            certificate
                .bytes
                .toUByteArray()
                .usePinned { pinned ->
                    if (pinned.get().isNotEmpty()) {
                        val isSuccessful = tls_config_install_certificate(
                            tlsConfig,
                            pinned.addressOf(0),
                            pinned.get().size.toULong()
                        )

                        if (!isSuccessful) throw IllegalArgumentException("Failed to install certificate. Invalid TLS certificate supplied: $certificate")
                    }
                }
        }
}