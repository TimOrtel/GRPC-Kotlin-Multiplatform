package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Certificate

class NativeCertificateRpcTest  : NativeJvmCertificateRpcTest() {

    override fun getCertificates(): List<Certificate> {
        val pemContent = """
        """.trimIndent()

        return listOf(
            Certificate.fromPem(pemContent)
        )
    }
}
