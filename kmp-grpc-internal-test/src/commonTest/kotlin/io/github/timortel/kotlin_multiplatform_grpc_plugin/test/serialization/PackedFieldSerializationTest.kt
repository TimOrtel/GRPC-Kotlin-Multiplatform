package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.test.NonPackedIntegersMessage
import io.github.timortel.kmpgrpc.test.NonPackedTypesMessage
import io.github.timortel.kmpgrpc.test.PackedIntegersMessage
import io.github.timortel.kmpgrpc.test.PackedTypesMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class PackedFieldSerializationTest {

    val field1 = listOf(0, 1, -13, 5000)
    val field2 = listOf(0L, 1L, -13L, 5000L)
    val field3 = listOf(0.0, 1.0, -13.0, 5000.0, -12.3, 31402.32)
    val field4 = listOf(0f, 1f, -13f, 5000f, -12.3f, 31402.32f)
    val field5 = listOf(0u, 1u, 13u, 5000u)
    val field6 = listOf(0uL, 1uL, 13uL, 5000uL)
    val field7 = listOf(0, 1, -13, 5000)
    val field8 = listOf(0L, 1L, -13L, 5000L)
    val field9 = listOf(0u, 1u, 13u, 5000u)
    val field10 = listOf(0uL, 1uL, 13uL, 5000uL)
    val field11 = listOf(0, 1, -13, 5000)
    val field12 = listOf(0L, 1L, -13L, 5000L)
    val field13 = listOf(true, false, false, true, true)

    val packedTypesMessage = PackedTypesMessage(
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
    val nonPackedTypesMessage = NonPackedTypesMessage(
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

    @Test
    fun testIntegerPackedSerializationToUnpackedDeserialization() {
        val msg = PackedIntegersMessage(field1List = field1)
        val expected = NonPackedIntegersMessage(field1List = field1)

        assertEquals(expected, NonPackedIntegersMessage.deserialize(msg.serialize()))
    }

    @Test
    fun testIntegerNonPackedSerializationToPackedDeserialization() {
        val msg = NonPackedIntegersMessage(field1List = field1)
        val expected = PackedIntegersMessage(field1List = field1)

        assertEquals(expected, PackedIntegersMessage.deserialize(msg.serialize()))
    }

    @Test
    fun testAllPackedSerializationToUnpackedDeserialization() {
        assertEquals(nonPackedTypesMessage, NonPackedTypesMessage.deserialize(packedTypesMessage.serialize()))
    }

    @Test
    fun testAllNonPackedSerializationToPackedDeserialization() {
        assertEquals(packedTypesMessage, PackedTypesMessage.deserialize(nonPackedTypesMessage.serialize()))
    }
}
