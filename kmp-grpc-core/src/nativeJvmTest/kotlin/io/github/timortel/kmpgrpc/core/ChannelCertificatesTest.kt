package io.github.timortel.kmpgrpc.core

import kotlin.test.Test
import kotlin.test.assertFailsWith

class ChannelCertificatesTest {

    @Test
    fun testEmptyCertificateThrowsError() {
        val emptyCert = Certificate.fromByteArray(ByteArray(0))

        assertFailsWith<IllegalArgumentException> {
            Channel.Builder
                .forAddress("localhost", 8443)
                .withTrustedCertificates(listOf(emptyCert))
                .build()
        }
    }

    @Test
    fun testInvalidCertificateThrowsError() {
        val invalidCert = Certificate.fromByteArray(byteArrayOf(1, 2, 3, 4, 5))

        assertFailsWith<IllegalArgumentException> {
            Channel.Builder
                .forAddress("localhost", 8443)
                .withTrustedCertificates(listOf(invalidCert))
                .build()
        }
    }

    @Test
    fun testInvalidClientIdentityThrowsError() {
        val cert = Certificate.fromPem("""
            -----BEGIN CERTIFICATE-----
            MIIBIjANBgkqhkiG9w==
            -----END CERTIFICATE-----
        """.trimIndent())

        val invalidKey = PrivateKey.fromByteArray(byteArrayOf(1, 2, 3))

        assertFailsWith<IllegalArgumentException> {
            Channel.Builder
                .forAddress("localhost", 8443)
                .withClientIdentity(cert, invalidKey)
                .build()
        }
    }
}
