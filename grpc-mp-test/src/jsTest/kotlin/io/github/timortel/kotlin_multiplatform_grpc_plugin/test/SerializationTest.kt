package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlin.test.Test
import kotlin.test.assertEquals

//actual fun reserializeLong(msg: KMLongMessage): KMLongMessage {
//    return KMLongMessage(JS_LongMessage.deserializeBinary(msg.jsImpl.serializeBinary()))
//}

class SerializationTest {

    @Test
    fun testSerializeLong() {
        val message = kmLongMessage {
            field1 = 12L
        }

        val binary = message.jsImpl.serializeBinary()
        val serializedMessage = KMLongMessage(JS_LongMessage.deserializeBinary(binary))

        assertEquals(message, serializedMessage)
    }

    @Test
    fun testSerializeRepeatedLong() {
        val message = kmRepeatedLongMessage {
            field1List += listOf(12L, 13L, 25L)
        }

        val binary = message.jsImpl.serializeBinary()
        val serializedMessage = KMRepeatedLongMessage(JS_RepeatedLongMessage.deserializeBinary(binary))

        assertEquals(message, serializedMessage)
    }

    @Test
    fun testScalarSerialization() {
        val msg = createScalarMessage()
        val data = msg.jsImpl.serializeBinary()
        val reconstructed = KMScalarTypes(JS_ScalarTypes.deserializeBinary(data))

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testSerializeMessageWithMessage() {
        val msg = kmMessageWithSubMessage {
            field1 = kmSimpleMessage { field1 = "Foo" }
        }

        val data = msg.jsImpl.serializeBinary()
        val reconstructed = KMMessageWithSubMessage(JS_MessageWithSubMessage.deserializeBinary(data))

        assertEquals(msg, reconstructed)
    }

    @Test
    fun testSerialization() {
        val msg = createMessageWithAllTypes()

        val data = msg.jsImpl.serializeBinary()
        val reconstructed = KMMessageWithEverything(JS_MessageWithEverything.deserializeBinary(data))

        assertEquals(msg, reconstructed)
    }
}