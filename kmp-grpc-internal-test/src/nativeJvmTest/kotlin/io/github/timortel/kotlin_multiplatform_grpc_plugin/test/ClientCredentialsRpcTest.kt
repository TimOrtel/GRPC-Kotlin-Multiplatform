package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import iio.github.timortel.kmpgrpc.internal.test.CA_CERTIFICATE
import iio.github.timortel.kmpgrpc.internal.test.CLIENT_CERTIFICATE
import iio.github.timortel.kmpgrpc.internal.test.CLIENT_KEY
import io.github.timortel.kmpgrpc.core.Certificate
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.PrivateKey
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kmpgrpc.test.SimpleMessage
import io.github.timortel.kmpgrpc.test.TestServiceStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.ServerTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClientCredentialsRpcTest : ServerTest {

    override val address: String
        get() = "localhost"

    override val port: Int
        get() = 17891

    private val channelWithCredentials = Channel.Builder.forAddress(address, port)
        .withTrustedCertificates(
            listOf(Certificate.fromPem(CA_CERTIFICATE.trimIndent()))
        )
        .withClientIdentity(
            certificate = Certificate.fromPem(CLIENT_CERTIFICATE.trimIndent()),
            key = PrivateKey.fromPem(CLIENT_KEY.trimIndent())
        )
        .build()

    private val channelWithoutCredentials = Channel.Builder.forAddress(address, port)
        .withTrustedCertificates(
            listOf(Certificate.fromPem(CA_CERTIFICATE.trimIndent()))
        )
        .build()

    @Test
    fun testConnectionWithCredentials() = runTest {
        val stub = TestServiceStub(channelWithCredentials)

        val msg = SimpleMessage(field1 = "Hello World")
        val response = stub.simpleRpc(msg)
        assertEquals(msg, response)
    }

    @Test
    fun testConnectionFailsWithoutCredentials() = runTest {
        val stub = TestServiceStub(channelWithoutCredentials)

        val msg = SimpleMessage(field1 = "Hello World")
        assertFailsWith<StatusException> { stub.simpleRpc(msg) }
    }
}
