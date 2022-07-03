package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.kmSimpleMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqTest {

    @Test
    fun scalarMessageEquals() {
        assertEquals(createScalarMessage(), createScalarMessage())
    }

    @Test
    fun scalarMessageDiffer() {
        val one = kmSimpleMessage {
            field1 = "Foo"
        }

        val two = kmSimpleMessage {
            field1 = "Bar"
        }

        assertNotEquals(one, two)
    }

    @Test
    fun messageWithAllTypesEquals() {
        assertEquals(createMessageWithAllTypes(), createMessageWithAllTypes())
    }
}