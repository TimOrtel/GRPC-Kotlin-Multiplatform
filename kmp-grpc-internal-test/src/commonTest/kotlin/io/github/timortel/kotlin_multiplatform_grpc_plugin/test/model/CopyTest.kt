package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import ExtensionsTest
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.core.message.extensions.buildExtensions
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

    @Test
    fun testCopyScalarMessageWithUnknownFieldsEquals() {
        val message = SimpleMessage(unknownFields = listOf(UnknownField.Varint(1, 1L)))
        assertEquals(message, message.copy())
    }

    @Test
    fun testCopyScalarMessageWithDifferentUnknownFieldsNotEquals() {
        val message = SimpleMessage(unknownFields = listOf(UnknownField.Varint(1, 1L)))
        assertNotEquals(message, message.copy(unknownFields = listOf(UnknownField.Varint(1, 2L))))
    }

    @Test
    fun testCopyExtensionMessageWithExtensionsEquals() {
        val message = ExtensionsTest.MessageWithExtension(
            extensions = buildExtensions {
                set(ExtensionsTest.extension, "field1")
            }
        )

        assertEquals(message, message.copy())
    }

    @Test
    fun testCopyExtensionMessageWithDifferentExtensionsNotEquals() {
        val message = ExtensionsTest.MessageWithExtension(
            extensions = buildExtensions {
                set(ExtensionsTest.extension, "value1")
            }
        )

        assertNotEquals(
            message,
            message.copy(
                extensions = buildExtensions {
                    set(ExtensionsTest.extension, "value2")
                }
            )
        )
    }
}
