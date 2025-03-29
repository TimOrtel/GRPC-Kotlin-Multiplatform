package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.emptyMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.simpleMessage
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
}