package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

class JsInterceptorTest : InterceptorTest() {
    override val address: String = "localhost"
    override val port: Int = 8082

    override val isJavaScript: Boolean
        get() = true
}
