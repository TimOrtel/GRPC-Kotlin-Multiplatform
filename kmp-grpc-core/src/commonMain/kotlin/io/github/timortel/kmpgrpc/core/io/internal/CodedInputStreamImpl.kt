// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

/*
This file is an adapted translation of: https://github.com/protocolbuffers/protobuf/blob/bf662dca814c7535b75d1520f9f32a76f7205c02/java/core/src/main/java/com/google/protobuf/CodedInputStream.java
Mainly the logic of reading varints and of checking and parsing input is copied.
Changes:
- Translated to Kotlin
- Removed unnecessary functionality
- Adapted to use kotlinx.io source.
- Adapted to throw custom errors.
 */

package io.github.timortel.kmpgrpc.core.io.internal

import io.github.timortel.kmpgrpc.core.io.*
import kotlinx.io.*

internal class CodedInputStreamImpl(val source: Source) : CodedInputStream() {

    private var lastTag: Int? = null

    private var position: Int = 0

    private var limit: Int = Int.MAX_VALUE

    override val isAtEnd: Boolean
        get() = source.exhausted() || position >= limit

    override fun readTag(): Int {
        if (isAtEnd) {
            lastTag = 0
            return 0
        }

        val tag = readVarInt32().toInt()
        lastTag = tag

        if (wireFormatGetTagFieldNumber(tag) == 0) {
            throw ParseException("Invalid 0 tag read.")
        }

        return tag
    }

    override fun checkLastTagWas(value: Int) {
        if (value != lastTag) throw ParseException("Expected tag $value but was $lastTag")
    }

    override fun skipField(tag: Int): Boolean {
        val wireFormat = getWireFormatByValue(wireFormatGetTagWireType(tag))
        when (wireFormat) {
            WireFormat.VARINT -> readVarInt64()
            WireFormat.FIXED64 -> skipImpl(8)
            WireFormat.FIXED32 -> skipImpl(4)
            WireFormat.LENGTH_DELIMITED -> {
                val length = readVarInt32()
                skipImpl(length.toInt())
            }

            WireFormat.START_GROUP -> {
                while (true) {
                    val newTag = readTag()
                    if (newTag == 0 || !skipField(newTag)) break
                }
                checkLastTagWas(wireFormatMakeTag(wireFormatGetTagFieldNumber(tag), WireFormat.END_GROUP))
            }

            WireFormat.END_GROUP -> {}
            null -> throw ParseException("Unknown wire format received.")
        }

        return wireFormat != WireFormat.END_GROUP
    }

    override fun readDouble(): Double {
        return Double.fromBits(readFixed64().toLong())
    }

    override fun readFloat(): Float {
        return Float.fromBits(readFixed32().toInt())
    }

    override fun readUInt64(): ULong {
        return readVarInt64()
    }

    override fun readInt64(): Long {
        return readVarInt64().toLong()
    }

    override fun readInt32(): Int {
        return readVarInt32().toInt()
    }

    override fun readFixed32(): UInt {
        position += 4
        return source.readIntLe().toUInt()
    }

    override fun readFixed64(): ULong {
        position += 8
        return source.readLongLe().toULong()
    }

    override fun readBool(): Boolean {
        return readVarInt64() != 0uL
    }

    override fun readString(): String {
        val length = readVarInt32()

        position += length.toInt()
        val bytes = source.readByteArray(length.toInt())
        return bytes.decodeToString()
    }

    override fun readBytes(): ByteArray {
        val size = readVarInt32().toInt()
        if (size < 0) throw ParseException.negativeSize(size)

        position += size
        return source.readByteArray(size)
    }

    override fun readUInt32(): UInt {
        return readVarInt32()
    }

    override fun readEnum(): Int {
        return readInt32()
    }

    override fun readSFixed32(): Int {
        position += 4
        return source.readIntLe()
    }

    override fun readSFixed64(): Long {
        position += 8
        return source.readLongLe()
    }

    override fun readSInt32(): Int {
        return DataDecoding.decodeZigZag32(readVarInt32().toInt())
    }

    override fun readSInt64(): Long {
        return DataDecoding.decodeZigZag64(readVarInt64().toLong())
    }

    override fun pushLimit(newLimit: Int): Int {
        if (newLimit < 0) {
            throw ParseException("New limit cannot be negative")
        }

        val proposedNewLimit = position + newLimit

        if (proposedNewLimit > this.limit) {
            throw ParseException("New limit cannot be greater than the old limit")
        }

        val oldLimit = limit
        limit = proposedNewLimit
        return oldLimit
    }

    override fun popLimit(oldLimit: Int) {
        limit = oldLimit
    }

    private fun readVarInt32(): UInt {
        return readVarInt64().toUInt()
    }

    private fun readVarInt64(): ULong {
        var result = 0uL
        var shift = 0
        while (shift < 64) {
            position++
            val b = source.readUByte()
            result = result or ((b and 0x7Fu).toULong() shl shift)
            if ((b.toInt() and 0x80) == 0) {
                return result
            }
            shift += 7
        }
        throw ParseException("Read malformed varint64")
    }

    private fun skipImpl(byteCount: Int) {
        source.skip(byteCount.toLong())
        position += byteCount
    }
}
