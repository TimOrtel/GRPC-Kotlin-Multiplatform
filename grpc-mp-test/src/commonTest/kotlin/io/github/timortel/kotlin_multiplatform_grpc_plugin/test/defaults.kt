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

    field19 = 12u
    field20 = 14uL
    field21 = 2421
    field22 = 1413414L
    field23 = 1245124u
    field24 = 124123122423123uL
    field25 = -13
    field26 = -1353532131L
    field27 = byteArrayOf(0, -13, 127)

    field28List += listOf(0u, 134u, 35311u)
    field29List += listOf(0uL, 134uL, 353111345134uL)
    field30List += listOf(-134, -145129, 34521431)
    field31List += listOf(-1L, 141341413413L, -134134314131L)
    field32List += listOf(0u, 14234u, 1413413413u)
    field33List += listOf(0uL, 134uL, 353111345134uL)
    field34List += listOf(-14, 0, 1241522)
    field35List += listOf(-154L, 0L, 4514124121L)
    field36List += listOf(byteArrayOf(0, -127, 127), byteArrayOf(-123, 1, 2), byteArrayOf(3, 3, -6))
}
