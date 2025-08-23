package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.test.Unknownfield
import io.github.timortel.kmpgrpc.test.emptyMessage
import io.github.timortel.kmpgrpc.test.simpleMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createMessageWithAllTypes
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createScalarMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqTest {

    @Test
    fun emptyMessageEquals() {
        assertEquals(emptyMessage { }, emptyMessage { })
    }

    @Test
    fun scalarMessageEquals() {
        assertEquals(createScalarMessage(), createScalarMessage())
    }

    @Test
    fun scalarMessageDiffer() {
        val one = simpleMessage {
            field1 = "Foo"
        }

        val two = simpleMessage {
            field1 = "Bar"
        }

        assertNotEquals(one, two)
    }

    @Test
    fun messageWithAllTypesEquals() {
        assertEquals(createMessageWithAllTypes(), createMessageWithAllTypes())
    }

    @Test
    fun messageWithUnknownFieldsEquals() {
        val message = Unknownfield.MessageWithUnknownField(
            a = "test.message",
            unknownFields = listOf(
                UnknownField.Varint(2, 13L),
                UnknownField.Fixed32(3, (-4f).toBits().toUInt()),
                UnknownField.Fixed64(4, (64.0).toBits().toULong()),
                UnknownField.LengthDelimited(5, "Test Message".encodeToByteArray())
            )
        )

        assertEquals(message, message)
    }

    @Test
    fun messageWithDifferentUnknownFieldsDiffer() {
        val message = Unknownfield.MessageWithUnknownField(
            a = "test.message",
            unknownFields = listOf(
                UnknownField.Varint(2, 13L),
                UnknownField.Fixed32(3, (-4f).toBits().toUInt())
            )
        )

        val message2 = Unknownfield.MessageWithUnknownField(
            a = "test.message",
            unknownFields = listOf(
                UnknownField.Varint(2, 13L),
                UnknownField.Fixed32(3, (-5f).toBits().toUInt()) // difference
            )
        )

        assertNotEquals(message, message2)
    }
}