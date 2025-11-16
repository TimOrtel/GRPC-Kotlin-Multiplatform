package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.ClientInterceptorImpl
import io.github.timortel.kmpgrpc.core.internal.buildSslSocketFactory
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import javax.net.ssl.SSLSocketFactory

/**
 * The Jvm [Channel] wraps the grpc [ManagedChannel] and delegates its operations to the wrapped native channel.
 */
actual class Channel private constructor(val channel: ManagedChannel) {
    actual class Builder(private val impl: OkHttpChannelBuilder) {

        private val trustedCertificates: MutableList<Certificate> = mutableListOf()
        private var trustOnlyProvidedCertificates = false
        private var customSslSocketFactory: SSLSocketFactory? = null

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder {
                return Builder(OkHttpChannelBuilder.forAddress(name, port))
            }
        }

        actual fun withInterceptors(vararg interceptors: CallInterceptor): Builder = apply {
            val grpcInterceptors = interceptors.map { ClientInterceptorImpl(it) }.toTypedArray()

            impl.intercept(*grpcInterceptors)
        }

        actual fun usePlaintext(): Builder = apply {
            impl.usePlaintext()
        }

        actual fun withTrustedCertificates(vararg certificates: Certificate): Builder {
            return withTrustedCertificates(certificates.toList())
        }

        actual fun withTrustedCertificates(certificates: List<Certificate>): Builder = apply {
            trustedCertificates += certificates
        }

        actual fun trustOnlyProvidedCertificates(): Builder = apply {
            trustOnlyProvidedCertificates = true
        }

        /**
         * Configure channel to use the provided [SSLSocketFactory]. Calling this function ignores the values set by [withTrustedCertificates].
         */
        fun useSslSocketFactory(sslSocketFactory: SSLSocketFactory): Builder = apply {
            customSslSocketFactory = sslSocketFactory
        }

        actual fun build(): Channel {
            impl.sslSocketFactory(
                customSslSocketFactory ?: buildSslSocketFactory(
                    certificates = trustedCertificates,
                    useDefaultTrustManager = !trustOnlyProvidedCertificates
                )
            )

            return Channel(impl.build())
        }
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
