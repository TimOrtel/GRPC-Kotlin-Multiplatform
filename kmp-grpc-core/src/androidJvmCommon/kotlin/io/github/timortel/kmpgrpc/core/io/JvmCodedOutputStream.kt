package io.github.timortel.kmpgrpc.core.io

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.Enum

internal class JvmCodedOutputStream(private val impl: CodedOutputStream) : IosJvmCodedOutputStream {

    override fun writeBool(fieldNumber: Int, value: Boolean) = impl.writeBool(fieldNumber, value)

    override fun writeBoolArray(fieldNumber: Int, value: List<Boolean>, tag: UInt): Unit =
        writeArray(
            fieldNumber,
            value,
            tag,
            CodedOutputStream::computeBoolSizeNoTag,
            ::writeBoolNoTag,
            ::writeBool
        )

    override fun writeBoolNoTag(value: Boolean) = impl.writeBoolNoTag(value)

    override fun writeBytes(fieldNumber: Int, value: ByteArray) {
        impl.writeBytes(fieldNumber, ByteString.copyFrom(value))
    }

    override fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>) {
        values.forEach { writeBytes(fieldNumber, it) }
    }

    override fun writeDouble(fieldNumber: Int, value: Double) = impl.writeDouble(fieldNumber, value)

    override fun writeDoubleArray(
        fieldNumber: Int,
        values: List<Double>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeDoubleSizeNoTag,
            ::writeDoubleNoTag,
            ::writeDouble
        )
    }

    override fun writeDoubleNoTag(value: Double) = impl.writeDoubleNoTag(value)

    override fun writeEnum(fieldNumber: Int, value: Int) = impl.writeEnum(fieldNumber, value)

    override fun writeEnum(fieldNumber: Int, value: Enum) = writeEnum(fieldNumber, value.number)

    override fun writeEnumArray(fieldNumber: Int, values: List<Enum>, tag: UInt) =
        writeArray(
            fieldNumber,
            values.map { it.number },
            tag,
            CodedOutputStream::computeEnumSizeNoTag,
            ::writeEnumNoTag,
            ::writeEnum
        )

    override fun writeEnumNoTag(value: Int) = impl.writeEnumNoTag(value)

    override fun writeFixed32(fieldNumber: Int, value: UInt) = impl.writeFixed32(fieldNumber, value.toInt())

    override fun writeFixed32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeFixed32SizeNoTag(it.toInt()) },
            ::writeFixed32NoTag,
            ::writeFixed32
        )
    }

    override fun writeFixed32NoTag(value: UInt) = impl.writeFixed32NoTag(value.toInt())

    override fun writeFixed64(fieldNumber: Int, value: ULong) = impl.writeFixed64(fieldNumber, value.toLong())

    override fun writeFixed64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeFixed64SizeNoTag(it.toLong()) },
            ::writeFixed64NoTag,
            ::writeFixed64
        )

    }

    override fun writeFixed64NoTag(value: ULong) = impl.writeFixed64NoTag(value.toLong())

    override fun writeFloat(fieldNumber: Int, value: Float) = impl.writeFloat(fieldNumber, value)

    override fun writeFloatArray(
        fieldNumber: Int,
        values: List<Float>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeFloatSizeNoTag,
            ::writeFloatNoTag,
            ::writeFloat
        )
    }

    override fun writeFloatNoTag(value: Float) = impl.writeFloatNoTag(value)

    override fun writeInt32(fieldNumber: Int, value: Int) = impl.writeInt32(fieldNumber, value)

    override fun writeInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeInt32SizeNoTag,
            ::writeInt32NoTag,
            ::writeInt32
        )
    }

    override fun writeInt32NoTag(value: Int) = impl.writeInt32NoTag(value)

    override fun writeInt64(fieldNumber: Int, value: Long) = impl.writeInt64(fieldNumber, value)

    override fun writeInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeInt64SizeNoTag,
            ::writeInt64NoTag,
            ::writeInt64
        )
    }

    override fun writeInt64NoTag(value: Long) = impl.writeInt64NoTag(value)

    override fun writeMessage(
        fieldNumber: Int,
        value: Message
    ) {
        writeMessage(fieldNumber, value, value.requiredSize.toUInt())
    }

    override fun writeMessageArray(
        fieldNumber: Int,
        values: List<Message>
    ) {
        writeMessageArray(fieldNumber, values) { it.requiredSize.toUInt() }
    }

    override fun writeRawVarint32(value: Int) = impl.writeUInt32NoTag(value)

    override fun writeSFixed32(fieldNumber: Int, value: Int) = impl.writeSFixed32(fieldNumber, value)

    override fun writeSFixed32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSFixed32SizeNoTag,
            ::writeSFixed32NoTag,
            ::writeSFixed32
        )
    }

    override fun writeSFixed32NoTag(value: Int) = impl.writeSFixed32NoTag(value)

    override fun writeSFixed64(fieldNumber: Int, value: Long) = impl.writeSFixed64(fieldNumber, value)

    override fun writeSFixed64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSFixed64SizeNoTag,
            ::writeSFixed64NoTag,
            ::writeSFixed64
        )
    }

    override fun writeSFixed64NoTag(value: Long) = impl.writeSFixed64NoTag(value)

    override fun writeSInt32(fieldNumber: Int, value: Int) = impl.writeSInt32(fieldNumber, value)

    override fun writeSInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSInt32SizeNoTag,
            ::writeSInt32NoTag,
            ::writeSInt32
        )
    }

    override fun writeSInt32NoTag(value: Int) = impl.writeSInt32NoTag(value)

    override fun writeSInt64(fieldNumber: Int, value: Long) = impl.writeSInt64(fieldNumber, value)

    override fun writeSInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSInt64SizeNoTag,
            ::writeSInt64NoTag,
            ::writeSInt64
        )
    }

    override fun writeSInt64NoTag(value: Long) = impl.writeSInt64NoTag(value)

    override fun writeString(fieldNumber: Int, value: String) = impl.writeString(fieldNumber, value)
    override fun writeStringArray(fieldNumber: Int, values: List<String>) {
        writeArray(
            fieldNumber,
            values,
            0u,
            CodedOutputStream::computeStringSizeNoTag,
            ::writeStringNoTag,
            ::writeString
        )
    }

    override fun writeStringNoTag(value: String) = impl.writeStringNoTag(value)

    override fun writeTag(
        fieldNumber: Int,
        format: WireFormat
    ) {
        //https://github.com/protocolbuffers/protobuf/blob/main/objectivec/GPBCodedOutputStream.m#L120
        impl.writeUInt32NoTag(wireFormatMakeTag(fieldNumber, format))
    }

    override fun writeUInt32(fieldNumber: Int, value: UInt) = impl.writeUInt32(fieldNumber, value.toInt())

    override fun writeUInt32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeUInt32SizeNoTag(it.toInt()) },
            ::writeUInt32NoTag,
            ::writeUInt32
        )
    }

    override fun writeUInt32NoTag(value: UInt) = impl.writeUInt32NoTag(value.toInt())

    override fun writeUInt64(fieldNumber: Int, value: ULong) = impl.writeUInt64(fieldNumber, value.toLong())

    override fun writeUInt64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) {
        writeArray(
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeUInt64SizeNoTag(it.toLong()) },
            ::writeUInt64NoTag,
            ::writeUInt64
        )

    }

    override fun writeUInt64NoTag(value: ULong) = impl.writeUInt64NoTag(value.toLong())
}
