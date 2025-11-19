package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Certificate
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.PrivateKey
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NativeCertificateConfigTest {

    @Test
    fun testEmptyCertificateThrowsError() {
        val emptyCert = Certificate.fromByteArray(ByteArray(0))

        val exception = assertFailsWith<IllegalArgumentException> {
            Channel.Builder
                .forAddress("localhost", 8443)
                .withTrustedCertificates(listOf(emptyCert))
                .build()
        }

        assertTrue(exception.message!!.contains("Certificate data is empty"))
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

