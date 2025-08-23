package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.ServerTest

interface ServerTestImpl : ServerTest {
    override val address: String
        get() = "localhost"

    override val port: Int
        get() = 17888
}
