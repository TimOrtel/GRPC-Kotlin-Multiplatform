package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.test.EditionsNonPackedIntegersMessage
import io.github.timortel.kmpgrpc.test.NonPackedIntegersMessage
import io.github.timortel.kmpgrpc.test.NonPackedTypesMessage
import io.github.timortel.kmpgrpc.test.EditionsNonPackedTypesMessage
import io.github.timortel.kmpgrpc.test.EditionsPackedIntegersMessage
import io.github.timortel.kmpgrpc.test.EditionsPackedTypesMessage
import io.github.timortel.kmpgrpc.test.PackedIntegersMessage
import io.github.timortel.kmpgrpc.test.PackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createEditionsNonPackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createEditionsPackedTypesMessage
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

    @Test
    fun testEditionIntegerPackedSerializationToUnpackedDeserialization() {
        val msg = EditionsPackedIntegersMessage(field1List = field1)
        val expected = EditionsNonPackedIntegersMessage(field1List = field1)

        assertEquals(expected, EditionsNonPackedIntegersMessage.deserialize(msg.serialize()))
    }

    @Test
    fun testEditionIntegerNonPackedSerializationToPackedDeserialization() {
        val msg = EditionsNonPackedIntegersMessage(field1List = field1)
        val expected = EditionsPackedIntegersMessage(field1List = field1)

        assertEquals(expected, EditionsPackedIntegersMessage.deserialize(msg.serialize()))
    }

    @Test
    fun testEditionAllPackedSerializationToUnpackedDeserialization() {
        assertEquals(createEditionsNonPackedTypesMessage(), EditionsNonPackedTypesMessage.deserialize(createEditionsPackedTypesMessage().serialize()))
    }

    @Test
    fun testEditionAllNonPackedSerializationToPackedDeserialization() {
        assertEquals(createEditionsPackedTypesMessage(), EditionsPackedTypesMessage.deserialize(createEditionsNonPackedTypesMessage().serialize()))
    }
}
