package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

class IosInterceptorTest : InterceptorTest() {
    override val address: String = "localhost"
    override val port: Int = 17888
}
