package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.Certificate
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

internal fun buildTrustedCertificatesSslSocketFactory(certificates: List<Certificate>): SSLSocketFactory {
    val certFactory = CertificateFactory.getInstance("X.509")

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null, null)
    }

    certificates.forEachIndexed { index, cert ->
        val cert = certFactory.generateCertificate(
            ByteArrayInputStream(cert.pemContent.toByteArray(Charsets.UTF_8))
        ) as X509Certificate
        keyStore.setCertificateEntry("cert-$index", cert)
    }

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(keyStore)

    // Step 5: SSLContext
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, tmf.trustManagers, null)

    return sslContext.socketFactory
}
