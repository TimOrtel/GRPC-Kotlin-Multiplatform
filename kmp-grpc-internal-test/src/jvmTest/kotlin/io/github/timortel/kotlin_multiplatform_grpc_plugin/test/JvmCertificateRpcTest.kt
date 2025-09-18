package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Certificate

class JvmCertificateRpcTest : NativeJvmCertificateRpcTest() {

    override fun getCertificates(): List<Certificate> {
        val pemContent = JvmCertificateRpcTest::class.java.classLoader.getResourceAsStream("server.pem")!!.use { input ->
            input.bufferedReader().use { reader ->
                reader.readText()
            }
        }

        return listOf(
            Certificate.fromPem(pemContent)
        )
    }
}
