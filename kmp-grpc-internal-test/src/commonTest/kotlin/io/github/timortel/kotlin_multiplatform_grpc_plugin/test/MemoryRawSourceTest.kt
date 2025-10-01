package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.internal.MemoryRawSource
import kotlinx.cinterop.allocArray
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import platform.posix.memcpy
import platform.posix.uint8_tVar
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryRawSourceTest {

    @Test
    fun testReadAtMostToPositionTracking() {
        val testData = "ABCDEFGHIJ".encodeToByteArray()
        val memory = allocArray<uint8_tVar>(testData.size)
        memcpy(memory, testData.refTo(0), testData.size.toULong())
        
        val source = MemoryRawSource(memory, testData.size.toULong())
        
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
