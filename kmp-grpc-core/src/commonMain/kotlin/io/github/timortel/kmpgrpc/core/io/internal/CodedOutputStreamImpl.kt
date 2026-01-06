// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

/*
 * This file is an adapted translation of: https://github.com/protocolbuffers/protobuf/blob/bf34ebee2801d089083977b087a2f6e65f3c7022/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java
 * Mainly the logic of writing varints and other types is taken.
 * Adapted:
 * - Extracted the write functions for the ArrayEncoder
 * - Translated to Kotlin
 * - Changed to use kotlinx-io sink
 */

package io.github.timortel.kmpgrpc.core.io.internal

import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.io.DataSize
import io.github.timortel.kmpgrpc.shared.internal.io.WireFormat
import io.github.timortel.kmpgrpc.core.io.kMapKeyFieldNumber
import io.github.timortel.kmpgrpc.core.io.kMapValueFieldNumber
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatMakeTag
import io.github.timortel.kmpgrpc.shared.internal.io.DataType
import io.github.timortel.kmpgrpc.core.message.Enum
import io.github.timortel.kmpgrpc.core.message.Message
import kotlinx.io.Sink
import kotlinx.io.writeString
import kotlinx.io.writeUByte
import kotlinx.io.writeUIntLe
import kotlinx.io.writeULongLe
import okio.utf8Size

internal class CodedOutputStreamImpl(private val sink: Sink) : CodedOutputStream {

    override fun writeBool(fieldNumber: Int, value: Boolean) {
        writeTag(fieldNumber, DataType.BOOL, false)
        writeBoolNoTag(value)
    }

    override fun writeBoolArray(fieldNumber: Int, values: List<Boolean>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeBoolSizeNoTag,
            writeWithTag = ::writeBool,
            writeWithoutTag = ::writeBoolNoTag
        )
    }

    override fun writeBoolNoTag(value: Boolean) {
        sink.writeUByte(if (value) 1u else 0u)
    }

    override fun writeBytes(fieldNumber: Int, value: ByteArray) {
        writeTag(fieldNumber, DataType.BYTES, true)
        writeVarInt32(value.size)
        sink.write(value)
    }

    override fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>) {
        values.forEach { writeBytes(fieldNumber, it) }
    }

    override fun writeDouble(fieldNumber: Int, value: Double) {
        writeTag(fieldNumber, DataType.DOUBLE, false)
        writeDoubleNoTag(value)
    }

    override fun writeDoubleArray(fieldNumber: Int, values: List<Double>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeDoubleSizeNoTag,
            writeWithTag = ::writeDouble,
            writeWithoutTag = ::writeDoubleNoTag
        )
    }

    override fun writeDoubleNoTag(value: Double) = writeFixed64NoTag(value.toRawBits().toULong())

    override fun writeEnum(fieldNumber: Int, value: Int) {
        writeTag(fieldNumber, DataType.ENUM, false)
        writeEnumNoTag(value)
    }

    override fun writeEnum(fieldNumber: Int, value: Enum) {
        writeEnum(fieldNumber, value.number)
    }

    override fun writeEnumArrayRaw(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeEnumSizeNoTag,
            writeWithTag = ::writeEnum,
            writeWithoutTag = ::writeEnumNoTag
        )
    }

    override fun writeEnumArray(
        fieldNumber: Int,
        values: List<Enum>,
        tag: UInt
    ) {
        writeEnumArrayRaw(fieldNumber = fieldNumber, values = values.map { it.number }, tag = tag)
    }

    override fun writeEnumNoTag(value: Int) = writeInt32NoTag(value)

    override fun writeFixed32(fieldNumber: Int, value: UInt) {
        writeTag(fieldNumber, DataType.FIXED32, false)
        writeFixed32NoTag(value)
    }

    override fun writeFixed32Array(fieldNumber: Int, values: List<UInt>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeFixed32SizeNoTag,
            writeWithTag = ::writeFixed32,
            writeWithoutTag = ::writeFixed32NoTag
        )
    }

    override fun writeFixed32NoTag(value: UInt) {
        sink.writeUIntLe(value)
    }

    override fun writeFixed64(fieldNumber: Int, value: ULong) {
        writeTag(fieldNumber, DataType.FIXED64, false)
        writeFixed64NoTag(value)
    }

    override fun writeFixed64Array(fieldNumber: Int, values: List<ULong>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeFixed64SizeNoTag,
            writeWithTag = ::writeFixed64,
            writeWithoutTag = ::writeFixed64NoTag
        )
    }

    override fun writeFixed64NoTag(value: ULong) {
        sink.writeULongLe(value)
    }

    override fun writeFloat(fieldNumber: Int, value: Float) {
        writeTag(fieldNumber, DataType.FLOAT, false)
        writeFloatNoTag(value)
    }

    override fun writeFloatArray(fieldNumber: Int, values: List<Float>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeFloatSizeNoTag,
            writeWithTag = ::writeFloat,
            writeWithoutTag = ::writeFloatNoTag
        )
    }

    override fun writeFloatNoTag(value: Float) = writeFixed32NoTag(value.toRawBits().toUInt())

    override fun writeInt32(fieldNumber: Int, value: Int) {
        writeTag(fieldNumber, DataType.INT32, false)
        writeInt32NoTag(value)
    }

    override fun writeInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeInt32SizeNoTag,
            writeWithTag = ::writeInt32,
            writeWithoutTag = ::writeInt32NoTag
        )
    }

    override fun writeInt32NoTag(value: Int) = writeVarInt32(value)

    override fun writeInt64(fieldNumber: Int, value: Long) {
        writeTag(fieldNumber, DataType.INT64, false)
        writeInt64NoTag(value)
    }

    override fun writeInt64Array(fieldNumber: Int, values: List<Long>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeInt64SizeNoTag,
            writeWithTag = ::writeInt64,
            writeWithoutTag = ::writeInt64NoTag
        )
    }

    override fun writeInt64NoTag(value: Long) = writeVarInt64(value)

    override fun writeMessage(fieldNumber: Int, value: Message) {
        writeUInt32NoTag(wireFormatMakeTag(fieldNumber, WireFormat.LENGTH_DELIMITED).toUInt())
        writeUInt32NoTag(value.requiredSize.toUInt())

        value.serialize(this)
    }

    override fun writeMessageArray(fieldNumber: Int, values: List<Message>) {
        values.forEach { writeMessage(fieldNumber, it) }
    }

    override fun writeGroup(fieldNumber: Int, value: Message) {
        writeTag(fieldNumber, WireFormat.START_GROUP)
        value.serialize(this)
        writeTag(fieldNumber, WireFormat.END_GROUP)
    }

    override fun writeGroupArray(fieldNumber: Int, values: List<Message>) {
        values.forEach { writeGroup(fieldNumber, it) }
    }

    override fun <K, V> writeMap(
        fieldNumber: Int,
        map: Map<K, V>,
        getKeySize: (Int, K) -> Int,
        getValueSize: (Int, V) -> Int,
        writeKey: CodedOutputStream.(Int, K) -> Unit,
        writeValue: CodedOutputStream.(Int, V) -> Unit
    ) {
        map.forEach { (key, value) ->
            // Write tag
            writeTag(fieldNumber, WireFormat.LENGTH_DELIMITED)
            // Write the size of the message
            val msgSize =
                getKeySize(kMapKeyFieldNumber, key) + getValueSize(kMapValueFieldNumber, value)
            writeInt32NoTag(msgSize)

            // Write fields
            writeKey(kMapKeyFieldNumber, key)
            writeValue(kMapValueFieldNumber, value)
        }
    }

    override fun writeSFixed32(fieldNumber: Int, value: Int) {
        writeTag(fieldNumber, DataType.SFIXED32, false)
        writeSFixed32NoTag(value)
    }

    override fun writeSFixed32Array(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeSFixed32SizeNoTag,
            writeWithTag = ::writeSFixed32,
            writeWithoutTag = ::writeSFixed32NoTag
        )
    }

    override fun writeSFixed32NoTag(value: Int) = writeFixed32NoTag(value.toUInt())

    override fun writeSFixed64(fieldNumber: Int, value: Long) {
        writeTag(fieldNumber, DataType.SFIXED64, false)
        writeSFixed64NoTag(value)
    }

    override fun writeSFixed64Array(fieldNumber: Int, values: List<Long>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeSFixed64SizeNoTag,
            writeWithTag = ::writeSFixed64,
            writeWithoutTag = ::writeSFixed64NoTag
        )
    }

    override fun writeSFixed64NoTag(value: Long) = writeFixed64NoTag(value.toULong())

    override fun writeSInt32(fieldNumber: Int, value: Int) {
        writeTag(fieldNumber, DataType.SINT32, false)
        writeSInt32NoTag(value)
    }

    override fun writeSInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeSInt32SizeNoTag,
            writeWithTag = ::writeSInt32,
            writeWithoutTag = ::writeSInt32NoTag
        )
    }

    override fun writeSInt32NoTag(value: Int) = writeUInt32NoTag(DataEncoding.encodeZigZag32(value))

    override fun writeSInt64(fieldNumber: Int, value: Long) {
        writeTag(fieldNumber, DataType.SINT64, false)
        writeSInt64NoTag(value)
    }

    override fun writeSInt64Array(fieldNumber: Int, values: List<Long>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeSInt64SizeNoTag,
            writeWithTag = ::writeSInt64,
            writeWithoutTag = ::writeSInt64NoTag
        )
    }

    override fun writeSInt64NoTag(value: Long) = writeUInt64NoTag(DataEncoding.encodeZigZag64(value))

    override fun writeString(fieldNumber: Int, value: String) {
        writeTag(fieldNumber, DataType.STRING, false)
        writeStringNoTag(value)
    }

    override fun writeStringArray(fieldNumber: Int, values: List<String>) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = 0u,
            computeSizeNoTag = DataSize::computeStringSizeNoTag,
            writeWithTag = ::writeString,
            writeWithoutTag = ::writeStringNoTag
        )
    }

    override fun writeStringNoTag(value: String) {
        writeVarUInt32(value.utf8Size().toUInt())
        sink.writeString(value)
    }

    override fun writeTag(fieldNumber: Int, format: WireFormat) {
        writeUInt32NoTag(wireFormatMakeTag(fieldNumber, format).toUInt())
    }

    override fun writeUInt32(fieldNumber: Int, value: UInt) {
        writeTag(fieldNumber, DataType.UINT32, false)
        writeUInt32NoTag(value)
    }

    override fun writeUInt32Array(fieldNumber: Int, values: List<UInt>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeUInt32SizeNoTag,
            writeWithTag = ::writeUInt32,
            writeWithoutTag = ::writeUInt32NoTag
        )
    }

    override fun writeUInt32NoTag(value: UInt) = writeVarUInt32(value)

    override fun writeUInt64(fieldNumber: Int, value: ULong) {
        writeTag(fieldNumber, DataType.UINT64, false)
        writeUInt64NoTag(value)
    }

    override fun writeUInt64Array(fieldNumber: Int, values: List<ULong>, tag: UInt) {
        writeArray(
            fieldNumber = fieldNumber,
            values = values,
            tag = tag,
            computeSizeNoTag = DataSize::computeUInt64SizeNoTag,
            writeWithTag = ::writeUInt64,
            writeWithoutTag = ::writeUInt64NoTag
        )
    }

    override fun writeUInt64NoTag(value: ULong) = writeVarUInt64(value)

    private fun writeTag(fieldNumber: Int, dataType: DataType, isPacked: Boolean) {
        writeVarInt32(wireFormatMakeTag(fieldNumber, dataType, isPacked))
    }

    private fun writeVarInt32(value: Int) {
        if (value >= 0) {
            writeVarUInt32(value.toUInt())
        } else {
            writeVarUInt64(value.toULong())
        }
    }

    private fun writeVarUInt32(value: UInt) {
        writeVarUInt64(value.toULong())
    }

    private fun writeVarInt64(value: Long) {
        writeVarUInt64(value.toULong())
    }

    private fun writeVarUInt64(value: ULong) {
        var currentValue = value

        while (true) {
            if ((currentValue and 0x7FuL.inv()) == 0uL) {
                sink.writeUByte((currentValue and 0xFFu).toUByte())
                return
            } else {
                sink.writeUByte(((currentValue and 0x7Fu) or 0x80u).toUByte())
                currentValue = currentValue shr 7
            }
        }
    }

    private fun <T> writeArray(
        fieldNumber: Int,
        values: Collection<T>,
        tag: UInt,
        computeSizeNoTag: (T) -> Int,
        writeWithTag: (Int, T) -> Unit,
        writeWithoutTag: (T) -> Unit
    ) {
        if (tag != 0u) {
            if (values.isEmpty()) return

            val dataSize = values.sumOf(computeSizeNoTag)
            writeVarUInt32(tag)
            writeVarInt32(dataSize)

            values.forEach(writeWithoutTag)
        } else {
            values.forEach { writeWithTag(fieldNumber, it) }
        }
    }
}
