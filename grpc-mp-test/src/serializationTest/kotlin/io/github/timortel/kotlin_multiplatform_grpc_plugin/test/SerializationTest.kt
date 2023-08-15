package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class SerializationTest {

    abstract fun serialize(message: KMLongMessage): KMLongMessage

    @Test
    fun testSerializeLong() {
        val message = kmLongMessage {
            field1 = 12L
        }

        val serializedMessage = serialize(message)

        assertEquals(message, serializedMessage)
    }

    abstract fun serialize(message: KMRepeatedLongMessage): KMRepeatedLongMessage

    @Test
    fun testSerializeRepeatedLong() {
        val message: KMRepeatedLongMessage = kmRepeatedLongMessage {
            field1List += listOf(12L, 13L, 25L)
        }

        val serializedMessage = serialize(message)

        assertEquals(message, serializedMessage)
    }

    abstract fun serialize(message: KMScalarTypes): KMScalarTypes

    @Test
    fun testScalarSerialization() {
        val msg: KMScalarTypes = createScalarMessage()
        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }

    abstract fun serialize(message: KMMessageWithSubMessage): KMMessageWithSubMessage

    @Test
    fun testComplexRepeatedSerialization() {
        val msg = createComplexRepeated()
        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }

    abstract fun serialize(message: KMComplexRepeatedMessage): KMComplexRepeatedMessage

    @Test
    fun testSerializeMessageWithMessage() {
        val msg: KMMessageWithSubMessage = kmMessageWithSubMessage {
            field1 = kmSimpleMessage { field1 = "Foo" }
        }

        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }

    abstract fun serialize(message: KMOneOfMessage): KMOneOfMessage

    @Test
    fun testSerializeOneOfScalarNumeric() {
        val msg = kmOneOfMessage {
            oneOf1 = KMOneOfMessage.OneOf1.Field1(23)
        }

        assertEquals(msg, serialize(msg))
    }

    @Test
    fun testSerializeOneOfMessage() {
        val msg = kmOneOfMessage {
            oneOf1 = KMOneOfMessage.OneOf1.Field3(kmLongMessage { field1 = 15323L })
        }

        assertEquals(msg, serialize(msg))
    }

    abstract fun serialize(message: KMMessageWithEverything): KMMessageWithEverything

    @Test
    fun testSerialization() {
        val msg: KMMessageWithEverything = createMessageWithAllTypes()

        val reconstructed = serialize(msg)

        assertEquals(msg, reconstructed)
    }
}