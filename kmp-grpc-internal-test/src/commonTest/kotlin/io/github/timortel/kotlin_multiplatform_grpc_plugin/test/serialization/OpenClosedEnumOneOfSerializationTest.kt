package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.test.editions.ClosedEnumTest
import io.github.timortel.kmpgrpc.test.editions.OpenEnumTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenClosedEnumOneOfSerializationTest {

    @Test
    fun testClosedOneOfDeserializationScenarios() {
        runClosedOneOfDeserializationTest(
            UnknownField.Varint(1, 0),
            ClosedEnumTest.MessageWithClosedOneOf.A.B(
                ClosedEnumTest.ClosedEnum.DEFAULT
            ),
            emptyList()
        )
        runClosedOneOfDeserializationTest(
            UnknownField.Varint(1, 1),
            ClosedEnumTest.MessageWithClosedOneOf.A.B(
                ClosedEnumTest.ClosedEnum.ONE
            ),
            emptyList()
        )
        runClosedOneOfDeserializationTest(
            UnknownField.Varint(1, 2),
            ClosedEnumTest.MessageWithClosedOneOf.A.NotSet,
            listOf(UnknownField.Varint(1, 2))
        )
    }

    private fun runClosedOneOfDeserializationTest(
        field: UnknownField.Varint,
        expectedValue: ClosedEnumTest.MessageWithClosedOneOf.A,
        expectedUnknownFields: List<UnknownField>
    ) {
        val msg = ClosedEnumTest.MessageWithClosedOneOf.deserialize(
            ClosedEnumTest.MessageWithClosedOneOf(unknownFields = listOf(field)).serialize()
        )

        assertEquals(expectedValue, msg.a, "Expected field value to be equal")
        assertEquals(expectedUnknownFields, msg.unknownFields, "Expected unknown fields to be equal")
    }

    @Test
    fun testOpenOneOfDeserializationScenarios() {
        runOpenOneOfDeserializationTest(
            UnknownField.Varint(1, 0),
            OpenEnumTest.MessageWithOpenOneOf.A.B(
                OpenEnumTest.OpenEnum.DEFAULT
            ),
            emptyList()
        )
        runOpenOneOfDeserializationTest(
            UnknownField.Varint(1, 1),
            OpenEnumTest.MessageWithOpenOneOf.A.B(
                OpenEnumTest.OpenEnum.ONE
            ),
            emptyList()
        )
        runOpenOneOfDeserializationTest(
            UnknownField.Varint(1, 2),
            OpenEnumTest.MessageWithOpenOneOf.A.B(OpenEnumTest.OpenEnum.UNRECOGNIZED),
            emptyList()
        )
    }

    private fun runOpenOneOfDeserializationTest(
        field: UnknownField.Varint,
        expectedValue: OpenEnumTest.MessageWithOpenOneOf.A,
        expectedUnknownFields: List<UnknownField>
    ) {
        val msg = OpenEnumTest.MessageWithOpenOneOf.deserialize(
            OpenEnumTest.MessageWithOpenOneOf(unknownFields = listOf(field)).serialize()
        )

        assertEquals(expectedValue, msg.a, "Expected field value to be equal")
        assertEquals(expectedUnknownFields, msg.unknownFields, "Expected unknown fields to be equal")
    }
}
