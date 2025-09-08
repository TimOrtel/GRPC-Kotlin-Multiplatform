package io.github.timortel.kmpgrpc.core.metadata

import io.github.timortel.kmpgrpc.core.message.FieldType
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.extensions.Extension
import io.github.timortel.kmpgrpc.core.message.extensions.buildExtensions
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageExtensionsBuilderTest {

    val extensionScalar = Extension.ScalarValueExtension(messageClass = Message::class, fieldNumber = 1, fieldType = FieldType.String)
    val extensionRepeatedNonPackable = Extension.NonPackableRepeatedValueExtension(messageClass = Message::class, fieldNumber = 2, fieldType = FieldType.String)
    val extensionRepeatedPackable = Extension.PackableRepeatedValueExtension(messageClass = Message::class, fieldNumber = 3, fieldType = FieldType.Int32, tag = 12u, isPacked = true)

    @Test
    fun test_WHEN_puttingValuesForExtensions_THEN_theExtensionsContainExactlyTheValuesWritten() {
        val scalarValue = "test1"
        val npValue = listOf("rt1", "rt2", "rt3")
        val pValue = listOf(12, -24, 124)

        val extensions = buildExtensions {
            put(extensionScalar, scalarValue)
            put(extensionRepeatedNonPackable, npValue)
            put(extensionRepeatedPackable, pValue)
        }

        assertEquals(scalarValue, extensions[extensionScalar])
        assertEquals(npValue, extensions[extensionRepeatedNonPackable])
        assertEquals(pValue, extensions[extensionRepeatedPackable])
    }

    @Test
    fun test_WHEN_putOrAppendCalledFirstTime_THEN_itInitializesWithValue() {
        val value = "first"

        val extensions = buildExtensions {
            putOrAppend(extensionRepeatedNonPackable, value)
        }

        assertEquals(listOf(value), extensions[extensionRepeatedNonPackable])
    }

    @Test
    fun test_WHEN_putOrAppendCalledMultipleTimes_THEN_itAppendsValues() {
        val values1 = listOf("a", "b")
        val values2 = listOf("c", "d")

        val extensions = buildExtensions {
            putOrAppend(extensionRepeatedNonPackable, values1)
            putOrAppend(extensionRepeatedNonPackable, values2)
        }

        assertEquals(listOf("a", "b", "c", "d"), extensions[extensionRepeatedNonPackable])
    }

    @Test
    fun test_WHEN_putAndThenPutOrAppend_THEN_putOrAppendAddsToExistingValues() {
        val initial = listOf(1, 2)
        val appended = listOf(3, 4)

        val extensions = buildExtensions {
            put(extensionRepeatedPackable, initial)
            putOrAppend(extensionRepeatedPackable, appended)
        }

        assertEquals(listOf(1, 2, 3, 4), extensions[extensionRepeatedPackable])
    }
}
