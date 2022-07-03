package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.KMSimpleEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.kmMessageWithEverything
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.kmScalarTypes
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.kmSimpleMessage

fun createScalarMessage() = kmScalarTypes {
    field1 = "Test"
    field2 = true
    field3 = 12
    field4 = 25L
    field5 = 3f
    field6 = 7.0
}

fun createMessageWithAllTypes() = kmMessageWithEverything {
    field1 = "Test"
    field2 = true
    field3 = 12
    field4 = 25L
    field5 = 3f
    field6 = 7.0
    field7 = KMSimpleEnum.ONE
    field8 = kmSimpleMessage { field1 = "Foo" }

    field9List += listOf("Foo", "Bar", "Baz")
    field10List += listOf(true, false, true, true)
    field11List += listOf(1, 2, 3, 4, -12, 1341)
    field12List += listOf(12L, 23424L, 10312313L, -123131L)
    field13List += listOf(-1f, 2f, 2.5f, -0.5f)
    field14List += listOf(-0.5, 15.0)
    field15List += listOf(KMSimpleEnum.ZERO, KMSimpleEnum.ZERO, KMSimpleEnum.ONE, KMSimpleEnum.TWO)

    field16Map += mapOf("foo" to 1, "bar" to -13, "baz" to 112)
    field17Map += mapOf(1 to kmSimpleMessage { field1 = "Foo" }, 13 to kmSimpleMessage { field1 = "Baz" })
    field18Map += mapOf(-15 to KMSimpleEnum.ONE, 23 to KMSimpleEnum.TWO)
}