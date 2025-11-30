package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.core.message.extensions.buildExtensions
import io.github.timortel.kmpgrpc.test.ComplexRepeatedMessage
import io.github.timortel.kmpgrpc.test.EditionsLegacyField
import io.github.timortel.kmpgrpc.test.LongMessage
import io.github.timortel.kmpgrpc.test.MessageWithEverything
import io.github.timortel.kmpgrpc.test.MessageWithSubMessage
import io.github.timortel.kmpgrpc.test.NonPackedTypesMessage
import io.github.timortel.kmpgrpc.test.OneOfMessage
import io.github.timortel.kmpgrpc.test.RepeatedLongMessage
import io.github.timortel.kmpgrpc.test.ScalarTypes
import io.github.timortel.kmpgrpc.test.SimpleMessage
import io.github.timortel.kmpgrpc.test.Unknownfield
import io.github.timortel.kmpgrpc.test.longMessage
import io.github.timortel.kmpgrpc.test.messageWithSubMessage
import io.github.timortel.kmpgrpc.test.oneOfMessage
import io.github.timortel.kmpgrpc.test.repeatedLongMessage
import io.github.timortel.kmpgrpc.test.simpleMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createComplexRepeated
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createMessageWithAllExtensions
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createMessageWithAllTypes
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createNonPackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createScalarMessage
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that messages when serialized and deserializes yield an equal message.
 */
class SelfMessageSerializationTest {

    @Test
    fun testSerializeLong() {
        val message = longMessage {
            field1 = 12L
        }

        val serializedMessage = LongMessage.deserialize(message.serialize())
        assertEquals(message, serializedMessage)
    }

    @Test
    fun testSerializeRepeatedLong() {
        val message: RepeatedLongMessage = repeatedLongMessage {
            field1List += listOf(12L, 13L, 25L)
        }

        val serializedMessage = RepeatedLongMessage.deserialize(message.serialize())

        assertEquals(message, serializedMessage)
    }

    @Test
    fun testScalarSerialization() {
        val msg: ScalarTypes = createScalarMessage()
        val reconstructed = ScalarTypes.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testComplexRepeatedSerialization() {
        val msg = createComplexRepeated()
        val reconstructed = ComplexRepeatedMessage.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testSerializeMessageWithMessage() {
        val msg: MessageWithSubMessage = messageWithSubMessage {
            field1 = simpleMessage { field1 = "Foo" }
        }

        val reconstructed = MessageWithSubMessage.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testSerializeOneOfScalarNumeric() {
        val msg = oneOfMessage {
            oneOf1 = OneOfMessage.OneOf1.Field1(23)
        }

        assertEquals(msg, OneOfMessage.deserialize(msg.serialize()))
    }

    @Test
    fun testSerializeOneOfMessage() {
        val msg = oneOfMessage {
            oneOf1 = OneOfMessage.OneOf1.Field3(longMessage { field1 = 15323L })
        }

        assertEquals(msg, OneOfMessage.deserialize(msg.serialize()))
    }

    @Test
    fun testSerialization() {
        val msg: MessageWithEverything = createMessageWithAllTypes()

        val reconstructed = MessageWithEverything.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testUnknownFieldsSerialization() {
        val fields = listOf(
            UnknownField.Varint(2, 13413L),
            UnknownField.Fixed32(3, 16531u),
            UnknownField.Fixed64(4, 165311414uL),
            UnknownField.LengthDelimited(5, byteArrayOf(12, -54, 5)),
            UnknownField.Group(6, listOf(UnknownField.Varint(7, 13L)))
        )

        val msg = Unknownfield.MessageWithUnknownField(unknownFields = fields)

        val reconstructed = Unknownfield.MessageWithUnknownField.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testNonPackedFieldsSerialization() {
        val msg = createNonPackedTypesMessage()

        val reconstructed = NonPackedTypesMessage.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testUtf8CharacterSerialization() {
        val text = "ś, ß, \uD83D\uDC4B".repeat(20)
        val msg = SimpleMessage(field1 = text)

        val reconstructed = SimpleMessage.deserialize(msg.serialize())

        assertEquals(text, reconstructed.field1)
        assertEquals(msg, reconstructed)
    }

    @Test
    fun testExtensionsSerialization() {
        val msg = ExtensionsTest.MessageWithExtension(
            extensions = buildExtensions {
                set(ExtensionsTest.extension, "test")
            }
        )

        val reconstructed = ExtensionsTest.MessageWithExtension.deserialize(
            msg.serialize(),
            ExtensionsTest.MessageWithExtension.defaultExtensionRegistry
        )

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testExtensionsSerializationEverything() {
        val msg = createMessageWithAllExtensions()

        val reconstructed = ExtensionsTest.MessageWithEveryExtension.deserialize(
            msg.serialize(),
            ExtensionsTest.MessageWithEveryExtension.defaultExtensionRegistry
        )

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testWrappedExtensionsSerializationEverything() {
        val msg = ExtensionsTest.MessageWithEveryExtensionWrapper(
            field1 = createMessageWithAllExtensions()
        )

        val reconstructed = ExtensionsTest.MessageWithEveryExtensionWrapper.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
        assertEquals(createMessageWithAllExtensions(), reconstructed.field1)
    }

    @Test
    fun testLegacyRequiredFieldSerialization() {
        val msg = EditionsLegacyField(a = 12)
        val reconstructed = EditionsLegacyField.deserialize(msg.serialize())

        assertEquals(msg, reconstructed)
    }
}
