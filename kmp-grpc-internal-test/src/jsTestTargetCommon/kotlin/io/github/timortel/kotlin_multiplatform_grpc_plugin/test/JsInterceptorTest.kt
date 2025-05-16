package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

class JsInterceptorTest : InterceptorTest(), ServerTestImpl {
    override val isJavaScript: Boolean
        get() = true
}
