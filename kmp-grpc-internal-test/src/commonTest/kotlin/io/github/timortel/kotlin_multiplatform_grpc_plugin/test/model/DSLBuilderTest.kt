package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.test.OneOfMessage
import io.github.timortel.kmpgrpc.test.SimpleEnum
import io.github.timortel.kmpgrpc.test.complexRepeatedMessage
import io.github.timortel.kmpgrpc.test.longMessage
import io.github.timortel.kmpgrpc.test.messageWithEnum
import io.github.timortel.kmpgrpc.test.messageWithEnumMap
import io.github.timortel.kmpgrpc.test.messageWithMap
import io.github.timortel.kmpgrpc.test.messageWithMessageMap
import io.github.timortel.kmpgrpc.test.messageWithNestedMessage
import io.github.timortel.kmpgrpc.test.messageWithRepeatedEnum
import io.github.timortel.kmpgrpc.test.messageWithRepeatedSubMessage
import io.github.timortel.kmpgrpc.test.messageWithSubMessage
import io.github.timortel.kmpgrpc.test.nestedMessage
import io.github.timortel.kmpgrpc.test.oneOfMessage
import io.github.timortel.kmpgrpc.test.simpleMessage
import io.github.timortel.kmpgrpc.test.simpleRepeatedMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createScalarMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DSLBuilderTest {

    @Test
    fun testBuildProtoObj() {
        val simpleMessage = simpleMessage {
            field1 = "Test"
        }

        assertEquals("Test", simpleMessage.field1)
    }

    @Test
    fun testScalarTypes() {
        val scalarMessage = createScalarMessage()

        assertEquals("Test", scalarMessage.field1)
        assertEquals(true, scalarMessage.field2)
        assertEquals(12, scalarMessage.field3)
        assertEquals(25L, scalarMessage.field4)
        assertEquals(3f, scalarMessage.field5)
        assertEquals(7.0, scalarMessage.field6)
    }

    @Test
    fun testCreateSimpleRepeated() {
        val list = listOf("Foo", "Bar", "Baz")

        val msg = simpleRepeatedMessage {
            field1List += list
        }

        assertEquals(list, msg.field1List)
    }

    @Test
    fun testCreateComplexRepeated() {
        val list1 = listOf("Foo", "Bar")
        val list2 = listOf(true, false, true, false)
        val list3 = listOf(12, 15, 27, -33)
        val list4 = listOf(125L, 1234324L, -23232L)
        val list5 = listOf(12f, 1.2f, 27f, 33f)
        val list6 = listOf(0.0, 1.0, 24.0, 1.5)

        val msg = complexRepeatedMessage {
            field1List += list1
            field2List += list2
            field3List += list3
            field4List += list4
            field5List += list5
            field6List += list6
        }

        assertEquals(list1, msg.field1List)
        assertEquals(list2, msg.field2List)
        assertEquals(list3, msg.field3List)
        assertEquals(list4, msg.field4List)
        assertEquals(list5, msg.field5List)
        assertEquals(list6, msg.field6List)
    }

    @Test
    fun testCreateMessageWithSubMessage() {
        val subMessage = simpleMessage { field1 = "Foo" }

        val msg = messageWithSubMessage {
            field1 = subMessage
        }

        assertEquals("Foo", subMessage.field1)
        assertEquals("Foo", msg.field1.field1)

        assertEquals(subMessage, msg.field1)
        assertEquals(subMessage.field1, msg.field1.field1)
    }

    @Test
    fun testCreateMessageWithRepeatedSubMessages() {
        val messages = listOf(
            simpleMessage { field1 = "Foo" },
            simpleMessage { field1 = "Bar" },
            simpleMessage { field1 = "Baz" },
        )

        val msg = messageWithRepeatedSubMessage {
            field1List += messages
        }

        assertEquals(messages, msg.field1List)
        messages.forEachIndexed { i, subMessage ->
            assertEquals(subMessage, msg.field1List[i])
        }
    }

    @Test
    fun testCreateEnumMessage() {
        val enum = SimpleEnum.ONE

        val msg = messageWithEnum {
            field1 = enum
        }

        assertEquals(enum, msg.field1)
    }

    @Test
    fun testCreateRepeatedEnumMessage() {
        val list = listOf(SimpleEnum.ONE, SimpleEnum.TWO, SimpleEnum.ZERO, SimpleEnum.ONE)

        val msg = messageWithRepeatedEnum {
            field1List += list
        }

        assertEquals(list, msg.field1List)
    }

    @Test
    fun testCreateMessageWithNestedMessage() {
        val nested = nestedMessage { field1 = 12 }

        val msg = messageWithNestedMessage {
            field1 = nested
        }

        assertEquals(nested, msg.field1)
        assertEquals(nested.field1, msg.field1.field1)
    }

    @Test
    fun testCreateMessageWithMap() {
        val map = mapOf(
            "Foo" to 12,
            "Bar" to 23,
            "Baz" to -13
        )

        val msg = messageWithMap {
            field1Map += map
        }

        assertEquals(map, msg.field1Map)

        map.entries.forEach { (key, value) ->
            assertTrue(msg.field1Map.containsKey(key))
            assertEquals(value, msg.field1Map[key])
        }
    }

    @Test
    fun testCreateMessageWithMessageMap() {
        val map = mapOf(
            12 to simpleMessage { field1 = "Foo" },
            22 to simpleMessage { field1 = "Baz" }
        )

        val msg = messageWithMessageMap {
            field1Map += map
        }

        assertEquals(map, msg.field1Map)

        map.entries.forEach { (key, value) ->
            assertTrue(msg.field1Map.containsKey(key))
            assertEquals(value, msg.field1Map[key])
        }
    }

    @Test
    fun testCreateMessageWithEnumMap() {
        val map = mapOf(
            33 to SimpleEnum.ONE,
            12 to SimpleEnum.TWO,
            13 to SimpleEnum.TWO,
            -12 to SimpleEnum.ONE,
            5 to SimpleEnum.ZERO
        )

        val msg = messageWithEnumMap {
            field1Map += map
        }

        assertEquals(map, msg.field1Map)
    }

    @Test
    fun testOneOfMessageBasic() {
        val value = OneOfMessage.OneOf1.Field1(13)

        val msg = oneOfMessage {
            oneOf1 = value
        }

        assertEquals(value, msg.oneOf1)
    }

    @Test
    fun testOneOfMessageMessage() {
        val longMessage = longMessage { field1 = 2521L }
        val value = OneOfMessage.OneOf1.Field3(longMessage)

        val msg = oneOfMessage {
            oneOf1 = value
        }

        assertEquals(value, msg.oneOf1)
        val oneOf = msg.oneOf1 as OneOfMessage.OneOf1.Field3
        assertEquals(longMessage, oneOf.field3)
    }

    @Test
    fun testOneOfMessageOverride() {
        val value = OneOfMessage.OneOf1.Field2("Test")

        val msg = oneOfMessage {
            oneOf1 = OneOfMessage.OneOf1.Field1(13)
            oneOf1 = value
        }

        assertEquals(value, msg.oneOf1)
    }
}