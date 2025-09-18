package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Certificate
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.test.SimpleMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.RpcTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class NativeJvmCertificateRpcTest : RpcTest() {

    override val address: String
        get() = "localhost"

    override val port: Int
        get() = 17889

    override fun buildChannel(builder: Channel.Builder) {
        builder.withTrustedCertificates(getCertificates())
    }

    abstract fun getCertificates(): List<Certificate>

    @Test
    fun testSelfSignedCertificateConnection() = runTest {
        val msg = SimpleMessage(field1 = "Hello World")
        val response = stub.simpleRpc(msg)
        assertEquals(msg, response)
    }
}
