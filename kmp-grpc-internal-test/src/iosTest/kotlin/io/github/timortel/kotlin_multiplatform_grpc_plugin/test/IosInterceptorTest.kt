package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

class IosInterceptorTest : IosJvmInterceptorTest() {
    override val address: String = "localhost"
    override val port: Int = 17888

    override val isJavaScript: Boolean
        get() = false
}
