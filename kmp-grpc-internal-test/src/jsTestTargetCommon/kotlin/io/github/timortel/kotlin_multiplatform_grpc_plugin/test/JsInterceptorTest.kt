package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.InterceptorTest

class JsInterceptorTest : InterceptorTest(), ServerTestImpl {
    override val isJavaScript: Boolean
        get() = true
}
