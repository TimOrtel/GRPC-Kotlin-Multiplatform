package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.JSPB
import kotlin.test.Test

class Foo {
    @Test
    fun foo() {
        val writer = JSPB.BinaryWriter()
        val encoder = writer.encoder
        println(encoder)
    }
}