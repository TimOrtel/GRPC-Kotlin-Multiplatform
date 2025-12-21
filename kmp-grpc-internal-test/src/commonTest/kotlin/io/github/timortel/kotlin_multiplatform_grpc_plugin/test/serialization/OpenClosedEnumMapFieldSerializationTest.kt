package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.test.editions.ClosedEnumTest
import io.github.timortel.kmpgrpc.test.editions.OpenEnumTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenClosedEnumMapFieldSerializationTest {

    @Test
    fun testClosedMapDeserializationScenarios() {
        runClosedMapDeserializationTest(
            fields = listOf(
                1 to 0,
                2 to 1
            ),
            expectedValues = mapOf(
                1 to ClosedEnumTest.ClosedEnum.DEFAULT,
                2 to ClosedEnumTest.ClosedEnum.ONE,
            ),
            expectedUnknownFieldCount = 0
        )

        runClosedMapDeserializationTest(
            fields = listOf(
                1 to -1,
                2 to 1,
                3 to 2
            ),
            expectedValues = mapOf(
                2 to ClosedEnumTest.ClosedEnum.ONE
            ),
            expectedUnknownFieldCount = 2
        )
    }

    private fun runClosedMapDeserializationTest(
        fields: List<Pair<Int, Int>>,
        expectedValues: Map<Int, ClosedEnumTest.ClosedEnum>,
        expectedUnknownFieldCount: Int
    ) {
        val dummyMessage = ClosedEnumTest.MessageWithDummyMap(
            aMap = fields.toMap()
        )

        val msg = ClosedEnumTest.MessageWithClosedEnumMap.deserialize(dummyMessage.serialize())

        assertEquals(expectedValues, msg.aMap, "Expected field values to be equal")
        assertEquals(expectedUnknownFieldCount, msg.unknownFields.size, "Expected unknown field count to be equal")
    }

    @Test
    fun testOpenMapDeserializationScenarios() {
        runOpenMapDeserializationTest(
            fields = listOf(
                1 to 0,
                2 to 1
            ),
            expectedValues = mapOf(
                1 to OpenEnumTest.OpenEnum.DEFAULT,
                2 to OpenEnumTest.OpenEnum.ONE,
            ),
            expectedUnknownFieldCount = 0
        )

        runOpenMapDeserializationTest(
            fields = listOf(
                1 to -1,
                2 to 1,
                3 to 2
            ),
            expectedValues = mapOf(
                1 to OpenEnumTest.OpenEnum.UNRECOGNIZED,
                2 to OpenEnumTest.OpenEnum.ONE,
                3 to OpenEnumTest.OpenEnum.UNRECOGNIZED
            ),
            expectedUnknownFieldCount = 0
        )
    }

    private fun runOpenMapDeserializationTest(
        fields: List<Pair<Int, Int>>,
        expectedValues: Map<Int, OpenEnumTest.OpenEnum>,
        expectedUnknownFieldCount: Int
    ) {
        val dummyMessage = ClosedEnumTest.MessageWithDummyMap(
            aMap = fields.toMap()
        )

        val msg = OpenEnumTest.MessageWithOpenEnumMap.deserialize(dummyMessage.serialize())

        assertEquals(expectedValues, msg.aMap, "Expected field values to be equal")
        assertEquals(expectedUnknownFieldCount, msg.unknownFields.size, "Expected unknown field count to be equal")
    }
}
