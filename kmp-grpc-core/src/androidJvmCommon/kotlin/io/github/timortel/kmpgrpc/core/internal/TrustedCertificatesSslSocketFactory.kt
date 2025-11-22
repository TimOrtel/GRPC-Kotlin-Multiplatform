package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.Certificate
import io.github.timortel.kmpgrpc.core.PrivateKey
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private val availableAlgorithms =
    Security.getProviders()
        .flatMap { it.services }
        .filter { it.type == "KeyPairGenerator" }
        .map { it.algorithm }

internal fun buildSslSocketFactory(
    certificates: List<Certificate>,
    useDefaultTrustManager: Boolean,
    keyManagers: Array<KeyManager>?
): SSLSocketFactory {
    val trustManager: X509TrustManager? = when {
        certificates.isNotEmpty() && useDefaultTrustManager -> CombinedTrustManager(
            trustedCertificatesTrustManager = buildTrustedCertificatesTrustManager(certificates),
            defaultTrustManager = getDefaultTrustManager()
        )

        certificates.isNotEmpty() -> buildTrustedCertificatesTrustManager(certificates)
        useDefaultTrustManager -> getDefaultTrustManager()
        else -> null
    }

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagers, if (trustManager != null) arrayOf(trustManager) else emptyArray(), null)

    return sslContext.socketFactory
}

@Suppress("CustomX509TrustManager")
private class CombinedTrustManager(
    private val trustedCertificatesTrustManager: X509TrustManager,
    private val defaultTrustManager: X509TrustManager
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        try {
            trustedCertificatesTrustManager.checkClientTrusted(chain, authType)
        } catch (_: Exception) {
            defaultTrustManager.checkClientTrusted(chain, authType)
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        try {
            trustedCertificatesTrustManager.checkServerTrusted(chain, authType)
        } catch (_: Exception) {
            defaultTrustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<out X509Certificate> =
        trustedCertificatesTrustManager.acceptedIssuers + defaultTrustManager.acceptedIssuers
}

private fun buildTrustedCertificatesTrustManager(certificates: List<Certificate>): X509TrustManager {
    val certFactory = CertificateFactory.getInstance("X.509")

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null, null)
    }

    certificates.forEachIndexed { index, cert ->
        val cert = parseCertificate(certFactory, cert)
        keyStore.setCertificateEntry("cert-$index", cert)
    }

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(keyStore)

    return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
}

private fun getDefaultTrustManager(): X509TrustManager {
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())

    tmf.init(null as KeyStore?)

    return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
}

internal fun buildClientIdentityKeyManager(cert: Certificate, key: PrivateKey): Array<KeyManager> {
    val keySpec = PKCS8EncodedKeySpec(key.data)
    val key = availableAlgorithms.asSequence().mapNotNull { algorithm ->
        try {
            KeyFactory.getInstance(algorithm).generatePrivate(keySpec)
        } catch (_: NoSuchAlgorithmException) {
            null
        } catch (_: InvalidKeySpecException) {
            null
        }
    }.firstOrNull() ?: throw IllegalArgumentException("Could not parse private key.")

    val certificate = parseCertificate(CertificateFactory.getInstance("X.509"), cert)

    val keyStore = KeyStore.getInstance("PKCS12")
    keyStore.load(null, null)
    keyStore.setKeyEntry("client", key, "".toCharArray(), arrayOf(certificate))

    val factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    factory.init(keyStore, "".toCharArray())

    return factory.keyManagers
}

private fun parseCertificate(certificateFactory: CertificateFactory, cert: Certificate): X509Certificate {
    return certificateFactory.generateCertificate(
        ByteArrayInputStream(cert.asPem.toByteArray(Charsets.UTF_8))
    ) as X509Certificate
}
