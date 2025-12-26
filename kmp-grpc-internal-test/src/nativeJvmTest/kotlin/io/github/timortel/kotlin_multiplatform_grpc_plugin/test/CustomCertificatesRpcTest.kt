package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.internal.test.CA_CERTIFICATE
import io.github.timortel.kmpgrpc.internal.test.STANDALONE_LEAF_CERTIFICATE
import io.github.timortel.kmpgrpc.core.Certificate
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kmpgrpc.test.SimpleMessage
import io.github.timortel.kmpgrpc.test.TestServiceStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.ServerTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class CustomCertificatesRpcTest : ServerTest {

    override val address: String
        get() = "localhost"

    private val channelWithCertificate = Channel.Builder.forAddress(address, port)
        .withTrustedCertificates(
            listOf(Certificate.fromPem(getCertificate().trimIndent()))
        )
        .build()

    private val channelWithoutCertificates = Channel.Builder.forAddress(address, port)
        .build()

    abstract fun getCertificate(): String

    @Test
    fun testConnectionWithCertificate() = runTest {
        val stub = TestServiceStub(channelWithCertificate)

        val msg = SimpleMessage(field1 = "Hello World")
        val response = stub.simpleRpc(msg)
        assertEquals(msg, response)
    }

    @Test
    fun testConnectionFailsWithoutCertificate() = runTest {
        val stub = TestServiceStub(channelWithoutCertificates)

        val msg = SimpleMessage(field1 = "Hello World")
        assertFailsWith<StatusException> { stub.simpleRpc(msg) }
    }
}

class CaCertCustomCertificatesRpcTest : CustomCertificatesRpcTest() {
    override val port: Int
        get() = 17889

    override fun getCertificate(): String = CA_CERTIFICATE
}

class StandaloneCertCustomCertificatesRpcTest : CustomCertificatesRpcTest() {
    override val port: Int
        get() = 17890

    override fun getCertificate(): String = STANDALONE_LEAF_CERTIFICATE
}
