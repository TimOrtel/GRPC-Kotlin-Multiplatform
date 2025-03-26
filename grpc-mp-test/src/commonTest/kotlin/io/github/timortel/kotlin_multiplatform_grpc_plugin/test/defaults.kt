package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*

fun createScalarMessage() = scalarTypes {
    field1 = "Test"
    field2 = true
    field3 = 12
    field4 = 25L
    field5 = 3f
    field6 = 7.0
}

fun createComplexRepeated() = complexRepeatedMessage {
    field1List += listOf("Foo", "Bar", "Baz")
    field2List += listOf(true, false, true)
    field3List += listOf(14, 142, 1, -35)
    field4List += listOf(12L, 23424L, 10312313L, -123131L)
    field5List += listOf(-1f, 2f, 2.5f, -0.5f)
    field6List += listOf(-0.5, 15.0)
}

fun createMessageWithAllTypes() = messageWithEverything {
    field1 = "Test"
    field2 = true
    field3 = 12
    field4 = 25L
    field5 = 3f
    field6 = 7.0
    field7 = SimpleEnum.ONE
    field8 = simpleMessage { field1 = "Foo" }

    field9List += listOf("Foo", "Bar", "Baz")
    field10List += listOf(true, false, true, true)
    field11List += listOf(1, 2, 3, 4, -12, 1341)
    field12List += listOf(12L, 23424L, 10312313L, -123131L)
    field13List += listOf(-1f, 2f, 2.5f, -0.5f)
    field14List += listOf(-0.5, 15.0)
    field15List += listOf(SimpleEnum.ZERO, SimpleEnum.ZERO, SimpleEnum.ONE, SimpleEnum.TWO)

    field16Map += mapOf("foo" to 1, "bar" to -13, "baz" to 112)
    field17Map += mapOf(1 to simpleMessage { field1 = "Foo" }, 13 to simpleMessage { field1 = "Baz" })
    field18Map += mapOf(-15 to SimpleEnum.ONE, 23 to SimpleEnum.TWO)
}
