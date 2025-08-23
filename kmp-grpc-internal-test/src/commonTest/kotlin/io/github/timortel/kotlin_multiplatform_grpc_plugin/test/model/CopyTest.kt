package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.test.SimpleMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CopyTest {

    @Test
    fun testCopyScalarMessageEquals() {
        val message = SimpleMessage(field1 = "test")
        assertEquals(message, message.copy())
    }

    @Test
    fun testCopyScalarMessageWithDifferentValuesNotEquals() {
        val message = SimpleMessage(field1 = "test")
        assertNotEquals(message, message.copy(field1 = "othertext"))
    }
}