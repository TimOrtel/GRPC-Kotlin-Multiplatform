package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.JSPB
import io.github.timortel.kmpgrpc.core.message.KMMessage
import io.github.timortel.kmpgrpc.core.message.KmEnum
import io.github.timortel.kmpgrpc.core.util.native

actual class CodedOutputStream(internal val impl: JSPB.BinaryWriter) {

    actual fun writeBool(fieldNumber: Int, value: Boolean) = impl.writeBool(fieldNumber, value)

    actual fun writeBoolArray(
        fieldNumber: Int,
        value: List<Boolean>,
        tag: UInt
    ) = writeArray(this, fieldNumber, value, tag, writeNoTag = ::writeBoolNoTag, writeWithTag = ::writeBool)

    actual fun writeBoolNoTag(value: Boolean) {
        impl.encoder.writeBool(value)
    }

    actual fun writeBytes(fieldNumber: Int, value: ByteArray) {
        impl.writeBytes(fieldNumber, value.native)
    }

    actual fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>) {
        val value = values.map { it.native }.toTypedArray()
        impl.writeRepeatedBytes(fieldNumber, value)
    }

    actual fun writeDouble(fieldNumber: Int, value: Double) {
        impl.writeDouble(fieldNumber, value)
    }

    actual fun writeDoubleArray(
        fieldNumber: Int,
        values: List<Double>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeDoubleNoTag, writeWithTag = ::writeDouble)

    actual fun writeDoubleNoTag(value: Double) {
        impl.encoder.writeDouble(value)
    }

    actual fun writeEnum(fieldNumber: Int, value: Int) {
        impl.writeEnum(fieldNumber, value)
    }

    actual fun writeEnum(fieldNumber: Int, value: KmEnum) = writeEnum(fieldNumber, value.number)

    actual fun writeEnumArray(fieldNumber: Int, values: List<KmEnum>, tag: UInt) =
        writeArray(
            this,
            fieldNumber,
            values.map { it.number },
            tag,
            writeNoTag = ::writeEnumNoTag,
            writeWithTag = ::writeEnum
        )

    actual fun writeEnumNoTag(value: Int) {
        impl.encoder.writeEnum(value)
    }

    actual fun writeFixed32(fieldNumber: Int, value: UInt) {
        impl.writeFixed32(fieldNumber, value)
    }

    actual fun writeFixed32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeFixed32NoTag, writeWithTag = ::writeFixed32)

    actual fun writeFixed32NoTag(value: UInt) {
        impl.encoder.writeUint32(value)
    }

    actual fun writeFixed64(fieldNumber: Int, value: ULong) {
        impl.writeFixed64(fieldNumber, value)
    }

    actual fun writeFixed64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeFixed64NoTag, writeWithTag = ::writeFixed64)

    actual fun writeFixed64NoTag(value: ULong) {
        impl.encoder.writeUint64(value)
    }

    actual fun writeFloat(fieldNumber: Int, value: Float) {
        impl.writeFloat(fieldNumber, value)
    }

    actual fun writeFloatArray(
        fieldNumber: Int,
        values: List<Float>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeFloatNoTag, writeWithTag = ::writeFloat)

    actual fun writeFloatNoTag(value: Float) {
        impl.encoder.writeFloat(value)
    }

    actual fun writeInt32(fieldNumber: Int, value: Int) {
        impl.writeInt32(fieldNumber, value)
    }

    actual fun writeInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeInt32NoTag, writeWithTag = ::writeInt32)

    actual fun writeInt32NoTag(value: Int) {
        impl.encoder.writeSignedVarint32(value)
    }

    actual fun writeInt64(fieldNumber: Int, value: Long) {
        impl.writeInt64(fieldNumber, value)
    }

    actual fun writeInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeInt64NoTag, writeWithTag = ::writeInt64)

    actual fun writeInt64NoTag(value: Long) {
        impl.encoder.writeSignedVarint64(value)
    }

    actual fun writeMessage(
        fieldNumber: Int,
        value: KMMessage
    ) {
        val bookmark = impl.beginDelimited(fieldNumber)
        value.serialize(this)
        impl.endDelimited(bookmark)
    }

    actual fun writeMessageArray(
        fieldNumber: Int,
        values: List<KMMessage>
    ) {
        values.forEach { message ->
            writeMessage(fieldNumber, message)
        }
    }

    actual fun writeRawVarint32(value: Int) {
        impl.encoder.writeUint32(value)
    }

    actual fun writeSFixed32(fieldNumber: Int, value: Int) {
        impl.writeSfixed32(fieldNumber, value)
    }

    actual fun writeSFixed32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSFixed32NoTag, writeWithTag = ::writeSFixed32)

    actual fun writeSFixed32NoTag(value: Int) {
        impl.encoder.writeInt32(value)
    }

    actual fun writeSFixed64(fieldNumber: Int, value: Long) {
        impl.writeSfixed64(fieldNumber, value)
    }

    actual fun writeSFixed64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSFixed64NoTag, writeWithTag = ::writeSFixed64)

    actual fun writeSFixed64NoTag(value: Long) {
        impl.encoder.writeInt64(value)
    }

    actual fun writeSInt32(fieldNumber: Int, value: Int) {
        impl.writeSint32(fieldNumber, value)
    }

    actual fun writeSInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSInt32NoTag, writeWithTag = ::writeSInt32)

    actual fun writeSInt32NoTag(value: Int) {
        impl.encoder.writeZigzagVarint32(value)
    }

    actual fun writeSInt64(fieldNumber: Int, value: Long) {
        impl.writeSint64(fieldNumber, value)
    }

    actual fun writeSInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSInt64NoTag, writeWithTag = ::writeSInt64)

    actual fun writeSInt64NoTag(value: Long) {
        impl.encoder.writeZigzagVarint64(value)
    }

    actual fun writeString(fieldNumber: Int, value: String) {
        impl.writeString(fieldNumber, value)
    }

    actual fun writeStringArray(
        fieldNumber: Int,
        values: List<String>
    ) {
        values.forEach { value -> writeString(fieldNumber, value) }
    }

    actual fun writeStringNoTag(value: String) {
        impl.encoder.writeString(value)
    }

    actual fun writeTag(
        fieldNumber: Int,
        format: WireFormat
    ) {
        impl.encoder.writeUnsignedVarint32(wireFormatMakeTag(fieldNumber, format))
    }

    actual fun writeUInt32(fieldNumber: Int, value: UInt) {
        impl.writeUint32(fieldNumber, value)
    }

    actual fun writeUInt32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeUInt32NoTag, writeWithTag = ::writeUInt32)

    actual fun writeUInt32NoTag(value: UInt) {
        impl.encoder.writeUnsignedVarint32(value)
    }

    actual fun writeUInt64(fieldNumber: Int, value: ULong) {
        impl.writeUint64(fieldNumber, value)
    }

    actual fun writeUInt64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeUInt64NoTag, writeWithTag = ::writeUInt64)

    actual fun writeUInt64NoTag(value: ULong) {
        impl.encoder.writeUnsignedVarint64(value)
    }
}