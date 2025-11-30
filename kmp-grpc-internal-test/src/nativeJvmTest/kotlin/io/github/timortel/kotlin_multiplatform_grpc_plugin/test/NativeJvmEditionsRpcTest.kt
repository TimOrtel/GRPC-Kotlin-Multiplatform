package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.EditionsRpcTest

class NativeJvmEditionsRpcTest : EditionsRpcTest() {
    override val address: String = "localhost"
    override val port: Int = 17892
}
