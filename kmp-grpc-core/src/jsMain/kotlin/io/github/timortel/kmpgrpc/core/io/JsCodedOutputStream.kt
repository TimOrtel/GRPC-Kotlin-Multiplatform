package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.external.JSPB
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.Enum
import io.github.timortel.kmpgrpc.core.native

/**
 * A Kotlin implementation of `CodedOutputStream` that wraps a `JSPB.BinaryWriter` object to facilitate
 * writing various types of data in a protocol buffer encoding.
 *
 * @constructor Initializes the class with an instance of `JSPB.BinaryWriter`.
 * @param impl An instance of `JSPB.BinaryWriter` used to perform the actual encoding.
 */
internal class JsCodedOutputStream(private val impl: JSPB.BinaryWriter) : CodedOutputStream {

    override fun writeBool(fieldNumber: Int, value: Boolean) = impl.writeBool(fieldNumber, value)

    override fun writeBoolArray(
        fieldNumber: Int,
        value: List<Boolean>,
        tag: UInt
    ) = writeArray(this, fieldNumber, value, tag, writeNoTag = ::writeBoolNoTag, writeWithTag = ::writeBool)

    override fun writeBoolNoTag(value: Boolean) {
        impl.encoder.writeBool(value)
    }

    override fun writeBytes(fieldNumber: Int, value: ByteArray) {
        impl.writeBytes(fieldNumber, value.native)
    }

    override fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>) {
        val value = values.map { it.native }.toTypedArray()
        impl.writeRepeatedBytes(fieldNumber, value)
    }

    override fun writeDouble(fieldNumber: Int, value: Double) {
        impl.writeDouble(fieldNumber, value)
    }

    override fun writeDoubleArray(
        fieldNumber: Int,
        values: List<Double>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeDoubleNoTag, writeWithTag = ::writeDouble)

    override fun writeDoubleNoTag(value: Double) {
        impl.encoder.writeDouble(value)
    }

    override fun writeEnum(fieldNumber: Int, value: Int) {
        impl.writeEnum(fieldNumber, value)
    }

    override fun writeEnum(fieldNumber: Int, value: Enum) = writeEnum(fieldNumber, value.number)

    override fun writeEnumArray(fieldNumber: Int, values: List<Enum>, tag: UInt) =
        writeArray(
            this,
            fieldNumber,
            values.map { it.number },
            tag,
            writeNoTag = ::writeEnumNoTag,
            writeWithTag = ::writeEnum
        )

    override fun writeEnumNoTag(value: Int) {
        impl.encoder.writeEnum(value)
    }

    override fun writeFixed32(fieldNumber: Int, value: UInt) {
        impl.writeFixed32(fieldNumber, value)
    }

    override fun writeFixed32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeFixed32NoTag, writeWithTag = ::writeFixed32)

    override fun writeFixed32NoTag(value: UInt) {
        impl.encoder.writeUint32(value)
    }

    override fun writeFixed64(fieldNumber: Int, value: ULong) {
        impl.writeFixed64(fieldNumber, value)
    }

    override fun writeFixed64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeFixed64NoTag, writeWithTag = ::writeFixed64)

    override fun writeFixed64NoTag(value: ULong) {
        impl.encoder.writeUint64(value)
    }

    override fun writeFloat(fieldNumber: Int, value: Float) {
        impl.writeFloat(fieldNumber, value)
    }

    override fun writeFloatArray(
        fieldNumber: Int,
        values: List<Float>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeFloatNoTag, writeWithTag = ::writeFloat)

    override fun writeFloatNoTag(value: Float) {
        impl.encoder.writeFloat(value)
    }

    override fun writeInt32(fieldNumber: Int, value: Int) {
        impl.writeInt32(fieldNumber, value)
    }

    override fun writeInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeInt32NoTag, writeWithTag = ::writeInt32)

    override fun writeInt32NoTag(value: Int) {
        impl.encoder.writeSignedVarint32(value)
    }

    override fun writeInt64(fieldNumber: Int, value: Long) {
        impl.writeInt64(fieldNumber, value)
    }

    override fun writeInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeInt64NoTag, writeWithTag = ::writeInt64)

    override fun writeInt64NoTag(value: Long) {
        impl.encoder.writeSignedVarint64(value)
    }

    override fun writeMessage(
        fieldNumber: Int,
        value: Message
    ) {
        val bookmark = impl.beginDelimited(fieldNumber)
        value.serialize(this)
        impl.endDelimited(bookmark)
    }

    override fun writeMessageArray(
        fieldNumber: Int,
        values: List<Message>
    ) {
        values.forEach { message ->
            writeMessage(fieldNumber, message)
        }
    }

    override fun writeRawVarint32(value: Int) {
        impl.encoder.writeUint32(value)
    }

    override fun writeSFixed32(fieldNumber: Int, value: Int) {
        impl.writeSfixed32(fieldNumber, value)
    }

    override fun writeSFixed32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSFixed32NoTag, writeWithTag = ::writeSFixed32)

    override fun writeSFixed32NoTag(value: Int) {
        impl.encoder.writeInt32(value)
    }

    override fun writeSFixed64(fieldNumber: Int, value: Long) {
        impl.writeSfixed64(fieldNumber, value)
    }

    override fun writeSFixed64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSFixed64NoTag, writeWithTag = ::writeSFixed64)

    override fun writeSFixed64NoTag(value: Long) {
        impl.encoder.writeInt64(value)
    }

    override fun writeSInt32(fieldNumber: Int, value: Int) {
        impl.writeSint32(fieldNumber, value)
    }

    override fun writeSInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSInt32NoTag, writeWithTag = ::writeSInt32)

    override fun writeSInt32NoTag(value: Int) {
        impl.encoder.writeZigzagVarint32(value)
    }

    override fun writeSInt64(fieldNumber: Int, value: Long) {
        impl.writeSint64(fieldNumber, value)
    }

    override fun writeSInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeSInt64NoTag, writeWithTag = ::writeSInt64)

    override fun writeSInt64NoTag(value: Long) {
        impl.encoder.writeZigzagVarint64(value)
    }

    override fun writeString(fieldNumber: Int, value: String) {
        impl.writeString(fieldNumber, value)
    }

    override fun writeStringArray(
        fieldNumber: Int,
        values: List<String>
    ) {
        values.forEach { value -> writeString(fieldNumber, value) }
    }

    override fun writeStringNoTag(value: String) {
        impl.encoder.writeString(value)
    }

    override fun writeTag(
        fieldNumber: Int,
        format: WireFormat
    ) {
        impl.encoder.writeUnsignedVarint32(wireFormatMakeTag(fieldNumber, format))
    }

    override fun writeUInt32(fieldNumber: Int, value: UInt) {
        impl.writeUint32(fieldNumber, value)
    }

    override fun writeUInt32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeUInt32NoTag, writeWithTag = ::writeUInt32)

    override fun writeUInt32NoTag(value: UInt) {
        impl.encoder.writeUnsignedVarint32(value)
    }

    override fun writeUInt64(fieldNumber: Int, value: ULong) {
        impl.writeUint64(fieldNumber, value)
    }

    override fun writeUInt64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) = writeArray(this, fieldNumber, values, tag, writeNoTag = ::writeUInt64NoTag, writeWithTag = ::writeUInt64)

    override fun writeUInt64NoTag(value: ULong) {
        impl.encoder.writeUnsignedVarint64(value)
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
            impl.beginSubMessage(fieldNumber)

            writeKey(kMapKeyFieldNumber, key)
            writeValue(kMapValueFieldNumber, value)

            impl.endSubMessage()
        }
    }

    /**
     * Writes a collection of values to a CodedOutputStream.
     */
    private fun <T> writeArray(
        stream: JsCodedOutputStream,
        fieldNumber: Int,
        values: Collection<T>,
        tag: UInt,
        writeNoTag: (T) -> Unit,
        writeWithTag: (Int, T) -> Unit
    ) {
        if (tag != 0u) {
            if (values.isEmpty()) return

            val mark = stream.impl.beginDelimited(fieldNumber)

            values.forEach(writeNoTag)

            stream.impl.endDelimited(mark)
        } else {
            values.forEach { writeWithTag(fieldNumber, it) }
        }
    }
}
