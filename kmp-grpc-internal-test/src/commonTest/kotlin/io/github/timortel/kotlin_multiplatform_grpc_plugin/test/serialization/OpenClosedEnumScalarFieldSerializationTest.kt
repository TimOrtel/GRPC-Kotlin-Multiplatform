package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.test.editions.closedtest.ClosedEnumTest
import io.github.timortel.kmpgrpc.test.editions.opentest.OpenEnumTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenClosedEnumScalarFieldSerializationTest {

    @Test
    fun testClosedScalarDeserializationScenarios() {
        runClosedScalarDeserializationTest(UnknownField.Varint(1, 0), ClosedEnumTest.ClosedEnum.DEFAULT, emptyList())
        runClosedScalarDeserializationTest(UnknownField.Varint(1, 1), ClosedEnumTest.ClosedEnum.ONE, emptyList())
        runClosedScalarDeserializationTest(
            UnknownField.Varint(1, 2),
            ClosedEnumTest.ClosedEnum.DEFAULT,
            listOf(UnknownField.Varint(1, 2))
        )
    }

    private fun runClosedScalarDeserializationTest(
        field: UnknownField.Varint,
        expectedValue: ClosedEnumTest.ClosedEnum,
        expectedUnknownFields: List<UnknownField>
    ) {
        val msg = ClosedEnumTest.MessageWithClosedEnum.deserialize(
            ClosedEnumTest.MessageWithClosedEnum(unknownFields = listOf(field)).serialize()
        )

        assertEquals(expectedValue, msg.a, "Expected field value to be equal")
        assertEquals(expectedUnknownFields, msg.unknownFields, "Expected unknown fields to be equal")
    }

    @Test
    fun testOpenScalarDeserializationScenarios() {
        runOpenScalarDeserializationTest(UnknownField.Varint(1, 0), OpenEnumTest.OpenEnum.DEFAULT, emptyList())
        runOpenScalarDeserializationTest(UnknownField.Varint(1, 1), OpenEnumTest.OpenEnum.ONE, emptyList())
        runOpenScalarDeserializationTest(
            UnknownField.Varint(1, 2),
            OpenEnumTest.OpenEnum.UNRECOGNIZED,
            emptyList()
        )
    }

    private fun runOpenScalarDeserializationTest(
        field: UnknownField.Varint,
        expectedValue: OpenEnumTest.OpenEnum,
        expectedUnknownFields: List<UnknownField>
    ) {
        val msg = OpenEnumTest.MessageWithOpenEnum.deserialize(
            OpenEnumTest.MessageWithOpenEnum(unknownFields = listOf(field)).serialize()
        )

        assertEquals(expectedValue, msg.a, "Expected field value to be equal")
        assertEquals(expectedUnknownFields, msg.unknownFields, "Expected unknown fields to be equal")
    }
}
