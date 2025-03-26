package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class SerializationTest {

    abstract fun serialize(message: LongMessage): LongMessage

    @Test
    fun testSerializeLong() {
        val message = longMessage {
            field1 = 12L
        }

        val serializedMessage = serialize(message)

        assertEquals(message, serializedMessage)
    }

    abstract fun serialize(message: RepeatedLongMessage): RepeatedLongMessage

    @Test
    fun testSerializeRepeatedLong() {
        val message: RepeatedLongMessage = repeatedLongMessage {
            field1List += listOf(12L, 13L, 25L)
        }

        val serializedMessage = serialize(message)

        assertEquals(message, serializedMessage)
    }

    abstract fun serialize(message: ScalarTypes): ScalarTypes

    @Test
    fun testScalarSerialization() {
        val msg: ScalarTypes = createScalarMessage()
        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }

    abstract fun serialize(message: MessageWithSubMessage): MessageWithSubMessage

    @Test
    fun testComplexRepeatedSerialization() {
        val msg = createComplexRepeated()
        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }

    abstract fun serialize(message: ComplexRepeatedMessage): ComplexRepeatedMessage

    @Test
    fun testSerializeMessageWithMessage() {
        val msg: MessageWithSubMessage = messageWithSubMessage {
            field1 = simpleMessage { field1 = "Foo" }
        }

        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }

    abstract fun serialize(message: OneOfMessage): OneOfMessage

    @Test
    fun testSerializeOneOfScalarNumeric() {
        val msg = oneOfMessage {
            oneOf1 = OneOfMessage.OneOf1.Field1(23)
        }

        assertEquals(msg, serialize(msg))
    }

    @Test
    fun testSerializeOneOfMessage() {
        val msg = oneOfMessage {
            oneOf1 = OneOfMessage.OneOf1.Field3(longMessage { field1 = 15323L })
        }

        assertEquals(msg, serialize(msg))
    }

    abstract fun serialize(message: MessageWithEverything): MessageWithEverything

    @Test
    fun testSerialization() {
        val msg: MessageWithEverything = createMessageWithAllTypes()

        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }
}