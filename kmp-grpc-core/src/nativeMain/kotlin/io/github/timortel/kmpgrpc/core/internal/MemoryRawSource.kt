package io.github.timortel.kmpgrpc.core.internal

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.writeUByte
import platform.posix.size_t
import platform.posix.uint8_tVar

/**
 * A raw memory-based implementation of the `RawSource` interface. This class reads data directly from
 * a specified memory region, identified by a pointer and size.
 *
 * @property pointer A `CPointer` pointing to the start of the memory region to be read.
 * @property size The size of the memory region, in bytes.
 */
internal class MemoryRawSource(
    val pointer: CPointer<uint8_tVar>,
    val size: size_t
) : RawSource {
    var position = 0uL

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        if (position >= size) return -1L

        val actualReadCount = minOf(byteCount.toULong(), size - position)
        val bytes = ByteArray(actualReadCount.toInt())
        
        for (i in 0 until actualReadCount.toInt()) {
            bytes[i] = pointer[(position + i.toULong()).toInt()]
        }
        
        sink.writeByteArray(bytes)

        position += actualReadCount
        return actualReadCount.toLong()
    }

    override fun close() = Unit
}
