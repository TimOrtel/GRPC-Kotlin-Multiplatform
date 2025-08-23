package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.test.NonPackedIntegersMessage
import io.github.timortel.kmpgrpc.test.NonPackedTypesMessage
import io.github.timortel.kmpgrpc.test.PackedIntegersMessage
import io.github.timortel.kmpgrpc.test.PackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createNonPackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createPackedTypesMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class PackedFieldSerializationTest {

    val field1 = listOf(0, 1, -13, 5000)

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
        assertEquals(createNonPackedTypesMessage(), NonPackedTypesMessage.deserialize(createPackedTypesMessage().serialize()))
    }

    @Test
    fun testAllNonPackedSerializationToPackedDeserialization() {
        assertEquals(createPackedTypesMessage(), PackedTypesMessage.deserialize(createNonPackedTypesMessage().serialize()))
    }
}
