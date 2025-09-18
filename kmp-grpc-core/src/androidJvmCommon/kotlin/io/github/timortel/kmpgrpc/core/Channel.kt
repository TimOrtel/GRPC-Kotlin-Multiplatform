package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.internal.ClientInterceptorImpl
import io.github.timortel.kmpgrpc.core.internal.buildTrustedCertificatesSslSocketFactory
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import javax.net.ssl.SSLSocketFactory

/**
 * The Jvm [Channel] wraps the grpc [ManagedChannel] and delegates its operations to the wrapped native channel.
 */
actual class Channel private constructor(val channel: ManagedChannel) {
    actual class Builder(private val impl: OkHttpChannelBuilder) {

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

        actual fun withTrustedCertificates(certificates: List<Certificate>): Builder {
            useSslSocketFactory(buildTrustedCertificatesSslSocketFactory(certificates))

            return this
        }

        fun useSslSocketFactory(sslSockerFactory: SSLSocketFactory): Builder {
            impl.sslSocketFactory(sslSockerFactory)
            return this
        }

        actual fun build(): Channel = Channel(impl.build())
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
