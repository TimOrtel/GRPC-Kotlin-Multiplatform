package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Certificate
import io.github.timortel.kmpgrpc.core.PrivateKey
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CertificateParsingTest {

    @Test
    fun testMalformedPemThrowsError() {
        assertFailsWith<IllegalArgumentException> {
            Certificate.fromPem("invalid PEM content")
        }
    }

    @Test
    fun testWrongContainerType() {
        val wrongContainerPem = """
            -----BEGIN PRIVATE KEY-----
            MIIBIjANBgkqhkiG9w==
            -----END PRIVATE KEY-----
        """.trimIndent()

        val exception = assertFailsWith<IllegalArgumentException> {
            Certificate.fromPem(wrongContainerPem)
        }
        assertTrue(exception.message!!.contains("expected CERTIFICATE"))
    }

    @Test
    fun testPrivateKeyWrongContainer() {
        val wrongContainerPem = """
            -----BEGIN CERTIFICATE-----
            MIIBIjANBgkqhkiG9w==
            -----END CERTIFICATE-----
        """.trimIndent()

        val exception = assertFailsWith<IllegalArgumentException> {
            PrivateKey.fromPem(wrongContainerPem)
        }
        assertTrue(exception.message!!.contains("expected PRIVATE KEY"))
    }
}

