package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DSLBuilderTest {

    @Test
    fun testBuildProtoObj() {
        val simpleMessage = kmSimpleMessage {
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

        val msg = kmSimpleRepeatedMessage {
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

        val msg = kmComplexRepeatedMessage {
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
        val subMessage = kmSimpleMessage { field1 = "Foo" }

        val msg = kmMessageWithSubMessage {
            field1 = subMessage
        }

        assertEquals(subMessage, msg.field1)
        assertEquals(subMessage.field1, msg.field1.field1)
    }

    @Test
    fun testCreateMessageWithRepeatedSubMessages() {
        val messages = listOf(
            kmSimpleMessage { field1 = "Foo" },
            kmSimpleMessage { field1 = "Bar" },
            kmSimpleMessage { field1 = "Baz" },
        )

        val msg = kmMessageWithRepeatedSubMessage {
            field1List += messages
        }

        assertEquals(messages, msg.field1List)
        messages.forEachIndexed { i, subMessage ->
            assertEquals(subMessage, msg.field1List[i])
        }
    }

    @Test
    fun testCreateEnumMessage() {
        val enum = KMSimpleEnum.ONE

        val msg = kmMessageWithEnum {
            field1 = enum
        }

        assertEquals(enum, msg.field1)
    }

    @Test
    fun testCreateRepeatedEnumMessage() {
        val list = listOf(KMSimpleEnum.ONE, KMSimpleEnum.TWO, KMSimpleEnum.ZERO, KMSimpleEnum.ONE)

        val msg = kmMessageWithRepeatedEnum {
            field1List += list
        }

        assertEquals(list, msg.field1List)
    }

    @Test
    fun testCreateMessageWithNestedMessage() {
        val nested = kmNestedMessage { field1 = 12 }

        val msg = kmMessageWithNestedMessage {
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

        val msg = kmMessageWithMap {
            field1Map += map
        }

        map.entries.forEach { (key, value) ->
            assertTrue(msg.field1Map.containsKey(key))
            assertEquals(value, msg.field1Map[key])
        }
    }

    @Test
    fun testCreateMessageWithMessageMap() {
        val map = mapOf(
            12 to kmSimpleMessage { field1 = "Foo" },
            22 to kmSimpleMessage { field1 = "Baz" }
        )

        val msg = kmMessageWithMessageMap {
            field1Map += map
        }

        map.entries.forEach { (key, value) ->
            assertTrue(msg.field1Map.containsKey(key))
            assertEquals(value, msg.field1Map[key])
        }
    }
}