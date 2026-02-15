package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import ExtensionsTest
import io.github.timortel.kmpgrpc.core.message.extensions.buildExtensions
import io.github.timortel.kmpgrpc.test.*
import io.github.timortel.kmpgrpc.test.proto2.Proto2GroupTest

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

fun createMessageWithAllExtensions() = ExtensionsTest.MessageWithEveryExtension(
    extensions = buildExtensions {
        set(ExtensionsTest.field1, "Test")
        set(ExtensionsTest.field2, true)
        set(ExtensionsTest.field3, 12)
        set(ExtensionsTest.field4, 25L)
        set(ExtensionsTest.field5, 3f)
        set(ExtensionsTest.field6, 7.0)
        set(ExtensionsTest.field7, SimpleEnum.ONE)
        set(ExtensionsTest.field8, simpleMessage { field1 = "Foo" })

        set(ExtensionsTest.field9, listOf("Foo", "Bar", "Baz"))
        set(ExtensionsTest.field10, listOf(true, false, true, true))
        set(ExtensionsTest.field11, listOf(1, 2, 3, 4, -12, 1341))
        set(ExtensionsTest.field12, listOf(12L, 23424L, 10312313L, -123131L))
        set(ExtensionsTest.field13, listOf(-1f, 2f, 2.5f, -0.5f))
        set(ExtensionsTest.field14, listOf(-0.5, 15.0))
        set(ExtensionsTest.field15, listOf(SimpleEnum.ZERO, SimpleEnum.ZERO, SimpleEnum.ONE, SimpleEnum.TWO))

        set(ExtensionsTest.field19, 12u)
        set(ExtensionsTest.field20, 14uL)
        set(ExtensionsTest.field21, 2421)
        set(ExtensionsTest.field22, 1413414L)
        set(ExtensionsTest.field23, 1245124u)
        set(ExtensionsTest.field24, 124123122423123uL)
        set(ExtensionsTest.field25, -13)
        set(ExtensionsTest.field26, -1353532131L)
        set(ExtensionsTest.field27, byteArrayOf(0, -13, 127))

        set(ExtensionsTest.field28, listOf(0u, 134u, 35311u))
        set(ExtensionsTest.field29, listOf(0uL, 134uL, 353111345134uL))
        set(ExtensionsTest.field30, listOf(-134, -145129, 34521431))
        set(ExtensionsTest.field31, listOf(-1L, 141341413413L, -134134314131L))
        set(ExtensionsTest.field32, listOf(0u, 14234u, 1413413413u))
        set(ExtensionsTest.field33, listOf(0uL, 134uL, 353111345134uL))
        set(ExtensionsTest.field34, listOf(-14, 0, 1241522))
        set(ExtensionsTest.field35, listOf(-154L, 0L, 4514124121L))
        set(
            ExtensionsTest.field36, listOf(
                byteArrayOf(0, -127, 127),
                byteArrayOf(-123, 1, 2),
                byteArrayOf(3, 3, -6)
            )
        )
    }
)


private val field1 = listOf(0, 1, -13, 5000)
private val field2 = listOf(0L, 1L, -13L, 5000L)
private val field3 = listOf(0.0, 1.0, -13.0, 5000.0, 2.5, -0.5)
private val field4 = listOf(0f, 1f, -13f, 5000f, 2.5f, -0.5f)
private val field5 = listOf(0u, 1u, 13u, 5000u)
private val field6 = listOf(0uL, 1uL, 13uL, 5000uL)
private val field7 = listOf(0, 1, -13, 5000)
private val field8 = listOf(0L, 1L, -13L, 5000L)
private val field9 = listOf(0u, 1u, 13u, 5000u)
private val field10 = listOf(0uL, 1uL, 13uL, 5000uL)
private val field11 = listOf(0, 1, -13, 5000)
private val field12 = listOf(0L, 1L, -13L, 5000L)
private val field13 = listOf(true, false, false, true, true)

fun createPackedTypesMessage(): PackedTypesMessage = PackedTypesMessage(
    field1List = field1,
    field2List = field2,
    field3List = field3,
    field4List = field4,
    field5List = field5,
    field6List = field6,
    field7List = field7,
    field8List = field8,
    field9List = field9,
    field10List = field10,
    field11List = field11,
    field12List = field12,
    field13List = field13,
)

fun createNonPackedTypesMessage(): NonPackedTypesMessage = NonPackedTypesMessage(
    field1List = field1,
    field2List = field2,
    field3List = field3,
    field4List = field4,
    field5List = field5,
    field6List = field6,
    field7List = field7,
    field8List = field8,
    field9List = field9,
    field10List = field10,
    field11List = field11,
    field12List = field12,
    field13List = field13,
)

fun createEditionsPackedTypesMessage(): EditionsPackedTypesMessage = EditionsPackedTypesMessage(
    field1List = field1,
    field2List = field2,
    field3List = field3,
    field4List = field4,
    field5List = field5,
    field6List = field6,
    field7List = field7,
    field8List = field8,
    field9List = field9,
    field10List = field10,
    field11List = field11,
    field12List = field12,
    field13List = field13,
)

fun createEditionsNonPackedTypesMessage(): EditionsNonPackedTypesMessage = EditionsNonPackedTypesMessage(
    field1List = field1,
    field2List = field2,
    field3List = field3,
    field4List = field4,
    field5List = field5,
    field6List = field6,
    field7List = field7,
    field8List = field8,
    field9List = field9,
    field10List = field10,
    field11List = field11,
    field12List = field12,
    field13List = field13,
)

fun createProto2NestedGroupMessage() = Proto2GroupTest.A(
    b = Proto2GroupTest.A.B(
        field1 = "text1",
        field2 = 2,
        c = Proto2GroupTest.A.B.C(field1 = "text3", field2 = "text4")
    ),
    dList = listOf(
        Proto2GroupTest.A.D(field1 = "text5", field2 = 6),
        Proto2GroupTest.A.D(field1 = "text7", field2 = 8)
    )
)

fun createProto2GroupMessageWithExtensions() = Proto2GroupTest.E(
    field1 = "text1",
    g = Proto2GroupTest.E.G(
        field1 = "text2",
        extensions = buildExtensions {
            set(Proto2GroupTest.field2, "text3")
        }
    ),
    extensions = buildExtensions {
        set(
            Proto2GroupTest.f, Proto2GroupTest.F(
                field1 = "text4"
            )
        )
    }
)
