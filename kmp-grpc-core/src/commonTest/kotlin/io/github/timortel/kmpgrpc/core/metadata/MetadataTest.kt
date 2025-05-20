package io.github.timortel.kmpgrpc.core.metadata

import kotlin.test.Test
import kotlin.test.assertEquals

class MetadataTest {

    @Test
    fun testCanRetrieveSingularAsciiKeyValuePair() {
        val key = Key.AsciiKey("key")
        val value = "value"

        val metadata = Metadata.of(key, value)
        assertEquals(value, metadata[key], "Expected key value pair to be present")
    }

    @Test
    fun testCanRetrieveSingularBinaryKeyValuePair() {
        val key = Key.BinaryKey("key-bin")
        val value = byteArrayOf(0, 14, -13, 127)

        val metadata = Metadata.of(key, value)
        assertEquals(value, metadata[key], "Expected key value pair to be present")
    }

    @Test
    fun testCanRetrieveMultipleAsciiKeyValuePairs() {
        val key = Key.AsciiKey("key")
        val values = setOf("value1", "value2")

        val metadata = Metadata.of(listOf(Entry.Ascii(key, values)))
        assertEquals(values, metadata.getAll(key), "Expected all key value pairs to be present")
    }

    @Test
    fun testCanRetrieveMultipleBinaryKeyValuePairs() {
        val key = Key.BinaryKey("key-bin")
        val values = setOf(byteArrayOf(0, 14, -13, 127), byteArrayOf(-14, 65, -23, 127))

        val metadata = Metadata.of(listOf(Entry.Binary(key, values)))
        assertEquals(values, metadata.getAll(key), "Expected all key value pairs to be present")
    }

    @Test
    fun testPlusNoClash() {
        val key = Key.AsciiKey("key")
        val values = setOf("value1", "value2", "value3")

        val metadata = values.fold(Metadata.empty()) { m, v -> m + Metadata.of(key, v) }

        assertEquals(values, metadata.getAll(key), "Expected all key value pairs to be present")
    }

    @Test
    fun testPlusWithClash() {
        val key = Key.AsciiKey("key")
        val values1 = setOf("value1", "value2", "value3")
        val values2 = setOf("value3", "value4", "value5")

        val metadata1 = Metadata.of(Entry.Ascii(key, values1))
        val metadata2 = Metadata.of(Entry.Ascii(key, values2))

        val finalMetadata = metadata1 + metadata2

        assertEquals(values1 + values2, finalMetadata.getAll(key), "Expected all key value pairs to be present")
    }

    @Test
    fun testMinus() {
        val key1 = Key.AsciiKey("key1")
        val key2 = Key.AsciiKey("key2")
        val values1 = setOf("value1", "value2", "value3")
        val values2 = setOf("value3", "value4", "value5")

        val metadata = Metadata.of(Entry.Ascii(key1, values1), Entry.Ascii(key2, values2))

        val finalMetadata = metadata - key1

        assertEquals(emptySet(), finalMetadata.getAll(key1), "Expected no values of key1 to be present")
        assertEquals(values2, finalMetadata.getAll(key2), "Expected all key value pairs to be present")
    }
}