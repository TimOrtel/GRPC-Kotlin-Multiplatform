package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import ExtensionsTest
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.core.message.extensions.buildExtensions
import io.github.timortel.kmpgrpc.test.Unknownfield
import io.github.timortel.kmpgrpc.test.emptyMessage
import io.github.timortel.kmpgrpc.test.proto2.Proto2GroupTest
import io.github.timortel.kmpgrpc.test.simpleMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createMessageWithAllExtensions
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createMessageWithAllTypes
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createProto2GroupMessageWithExtensions
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createProto2NestedGroupMessage
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

    @Test
    fun messageWithDifferentExtensionsEqual() {
        val msg1 = createMessageWithAllExtensions()
        val msg2 = createMessageWithAllExtensions()

        assertEquals(msg1, msg2)
    }

    @Test
    fun messageWithDifferentExtensionsDiffer() {
        val msg1 = createMessageWithAllExtensions()
        val msg2 = ExtensionsTest.MessageWithEveryExtension(
            extensions = buildExtensions {
                set(ExtensionsTest.field1, "Foo")
            }
        )

        assertNotEquals(msg1, msg2)
    }

    @Test
    fun messageWithNestedGroupsEqual() {
        val msg1 = createProto2NestedGroupMessage()
        val msg2 = createProto2NestedGroupMessage()

        assertEquals(msg1, msg2)
    }

    @Test
    fun messageWithNestedGroupsDiffer() {
        val msg1 = createProto2NestedGroupMessage()
        val msg2 = Proto2GroupTest.A()

        assertNotEquals(msg1, msg2)
    }

    @Test
    fun messageWithGroupMessageExtensionsEqual() {
        val msg1 = createProto2GroupMessageWithExtensions()
        val msg2 = createProto2GroupMessageWithExtensions()

        assertEquals(msg1, msg2)
    }

    @Test
    fun messageWithGroupMessageExtensionsDiffer() {
        val msg1 = createProto2GroupMessageWithExtensions()
        val msg2 = msg1.copy(extensions = buildExtensions {  })

        assertNotEquals(msg1, msg2)
    }
}
