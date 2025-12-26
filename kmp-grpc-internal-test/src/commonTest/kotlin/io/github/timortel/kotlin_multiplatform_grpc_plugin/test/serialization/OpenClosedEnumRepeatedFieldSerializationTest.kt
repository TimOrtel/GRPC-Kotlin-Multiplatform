package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.test.editions.closedtest.ClosedEnumTest
import io.github.timortel.kmpgrpc.test.editions.opentest.OpenEnumTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class OpenClosedEnumRepeatedFieldSerializationTest {

    @Test
    fun testClosedRepeatedDeserializationScenarios() {
        runClosedRepeatedDeserializationTest(
            fields = listOf(0),
            expectedValues = listOf(ClosedEnumTest.ClosedEnum.DEFAULT),
            expectedUnknownFields = emptyList()
        )

        runClosedRepeatedDeserializationTest(
            fields = listOf(0, 1, 0),
            expectedValues = listOf(
                ClosedEnumTest.ClosedEnum.DEFAULT,
                ClosedEnumTest.ClosedEnum.ONE,
                ClosedEnumTest.ClosedEnum.DEFAULT
            ),
            expectedUnknownFields = emptyList()
        )

        runClosedRepeatedDeserializationTest(
            fields = listOf(-4, 3, 1),
            expectedValues = listOf(
                ClosedEnumTest.ClosedEnum.ONE
            ),
            expectedUnknownFields = listOf(
                UnknownField.Varint(1, -4),
                UnknownField.Varint(1, 3),
                UnknownField.Varint(2, -4),
                UnknownField.Varint(2, 3),
            )
        )
    }

    private fun runClosedRepeatedDeserializationTest(
        fields: List<Long>,
        expectedValues: List<ClosedEnumTest.ClosedEnum>,
        expectedUnknownFields: List<UnknownField>
    ) {
        val unknownFields = fields.flatMap {
            listOf(
                UnknownField.Varint(1, it),
                UnknownField.Varint(2, it)
            )
        }

        val msg = ClosedEnumTest.MessageWithRepeatedClosedEnum.deserialize(
            ClosedEnumTest.MessageWithRepeatedClosedEnum(unknownFields = unknownFields).serialize()
        )

        assertEquals(expectedValues, msg.aList, "Expected field A values to be equal")
        assertEquals(expectedValues, msg.bList, "Expected field B values to be equal")
        expectedUnknownFields.forEach {
            assertContains(msg.unknownFields, it, "Expected to be contained")
        }
    }

    @Test
    fun testOpenRepeatedDeserializationScenarios() {
        runOpenRepeatedDeserializationTest(
            fields = listOf(0),
            expectedValues = listOf(OpenEnumTest.OpenEnum.DEFAULT),
            expectedUnknownFields = emptyList()
        )

        runOpenRepeatedDeserializationTest(
            fields = listOf(0, 1, 0),
            expectedValues = listOf(
                OpenEnumTest.OpenEnum.DEFAULT,
                OpenEnumTest.OpenEnum.ONE,
                OpenEnumTest.OpenEnum.DEFAULT
            ),
            expectedUnknownFields = emptyList()
        )

        runOpenRepeatedDeserializationTest(
            fields = listOf(-4, 3, 1),
            expectedValues = listOf(
                OpenEnumTest.OpenEnum.Unrecognized(-4),
                OpenEnumTest.OpenEnum.Unrecognized(3),
                OpenEnumTest.OpenEnum.ONE,
            ),
            expectedUnknownFields = emptyList()
        )
    }

    private fun runOpenRepeatedDeserializationTest(
        fields: List<Long>,
        expectedValues: List<OpenEnumTest.OpenEnum>,
        expectedUnknownFields: List<UnknownField>
    ) {
        val unknownFields = fields.map { UnknownField.Varint(1, it) }

        val msg = OpenEnumTest.MessageWithRepeatedOpenEnum.deserialize(
            OpenEnumTest.MessageWithRepeatedOpenEnum(unknownFields = unknownFields).serialize()
        )

        assertEquals(expectedValues, msg.aList, "Expected field values to be equal")
        assertEquals(expectedUnknownFields, msg.unknownFields, "Expected unknown fields to be equal")
    }
}
