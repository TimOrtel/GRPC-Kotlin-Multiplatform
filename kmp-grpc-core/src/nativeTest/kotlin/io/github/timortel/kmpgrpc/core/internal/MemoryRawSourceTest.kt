package io.github.timortel.kmpgrpc.core.internal

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryRawSourceTest {

    @Test
    fun testRepeatedReads() {
        "ABCDEFGHIJ".encodeToByteArray().toUByteArray().usePinned {
            val source = MemoryRawSource(it.addressOf(0), it.get().size.toULong())

            // First read
            val sink1 = Buffer()
            val bytesRead1 = source.readAtMostTo(sink1, 5L)
            assertEquals(5L, bytesRead1)
            assertEquals("ABCDE".encodeToByteArray().toList(), sink1.readByteArray().toList())

            // Second read - should continue from position 5
            val sink2 = Buffer()
            val bytesRead2 = source.readAtMostTo(sink2, 5L)
            assertEquals(5L, bytesRead2)
            assertEquals("FGHIJ".encodeToByteArray().toList(), sink2.readByteArray().toList())
        }
    }
}
