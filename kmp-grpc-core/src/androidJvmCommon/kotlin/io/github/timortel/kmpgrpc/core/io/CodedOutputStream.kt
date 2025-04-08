package io.github.timortel.kmpgrpc.core.io

import com.google.protobuf.ByteString
import io.github.timortel.kmpgrpc.core.message.KMMessage
import io.github.timortel.kmpgrpc.core.message.KmEnum
import io.github.timortel.kmpgrpc.core.message.requiredSizeMessage
import io.github.timortel.kmpgrpc.core.message.serializeMessage
import com.google.protobuf.CodedOutputStream

actual class CodedOutputStream(private val impl: com.google.protobuf.CodedOutputStream) {

    private fun <T> writeArray(
        stream: io.github.timortel.kmpgrpc.core.io.CodedOutputStream,
        fieldNumber: Int,
        values: Collection<T>,
        tag: UInt,
        computeSizeNoTag: (T) -> Int,
        writeNoTag: (T) -> Unit,
        writeTag: (Int, T) -> Unit
    ) = io.github.timortel.kmpgrpc.core.io.writeArray(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeSizeNoTag = { computeSizeNoTag(it).toULong() },
        writeNoTag = writeNoTag,
        writeTag = writeTag
    )

    actual fun writeBool(fieldNumber: Int, value: Boolean) = impl.writeBool(fieldNumber, value)

    actual fun writeBoolArray(fieldNumber: Int, value: List<Boolean>, tag: UInt): Unit =
        writeArray(
            this,
            fieldNumber,
            value,
            tag,
            CodedOutputStream::computeBoolSizeNoTag,
            ::writeBoolNoTag,
            ::writeBool
        )

    actual fun writeBoolNoTag(value: Boolean) = impl.writeBoolNoTag(value)

    actual fun writeBytes(fieldNumber: Int, value: ByteArray) {
        impl.writeBytes(fieldNumber, ByteString.copyFrom(value))
    }

    actual fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>) {
        values.forEach { writeBytes(fieldNumber, it) }
    }

    actual fun writeBytesNoTag(value: ByteArray) {
        impl.writeBytesNoTag(ByteString.copyFrom(value))
    }

    actual fun writeDouble(fieldNumber: Int, value: Double) = impl.writeDouble(fieldNumber, value)

    actual fun writeDoubleArray(
        fieldNumber: Int,
        values: List<Double>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeDoubleSizeNoTag,
            ::writeDoubleNoTag,
            ::writeDouble
        )
    }

    actual fun writeDoubleNoTag(value: Double) = impl.writeDoubleNoTag(value)

    actual fun writeEnum(fieldNumber: Int, value: Int) = impl.writeEnum(fieldNumber, value)

    actual fun writeEnum(fieldNumber: Int, value: KmEnum) = writeEnum(fieldNumber, value.number)

    actual fun writeEnumArray(fieldNumber: Int, values: List<KmEnum>, tag: UInt) =
        writeArray(
            this,
            fieldNumber,
            values.map { it.number },
            tag,
            CodedOutputStream::computeEnumSizeNoTag,
            ::writeEnumNoTag,
            ::writeEnum
        )

    actual fun writeEnumNoTag(value: Int) = impl.writeEnumNoTag(value)

    actual fun writeFixed32(fieldNumber: Int, value: UInt) = impl.writeFixed32(fieldNumber, value.toInt())

    actual fun writeFixed32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeFixed32SizeNoTag(it.toInt()) },
            ::writeFixed32NoTag,
            ::writeFixed32
        )
    }

    actual fun writeFixed32NoTag(value: UInt) = impl.writeFixed32NoTag(value.toInt())

    actual fun writeFixed64(fieldNumber: Int, value: ULong) = impl.writeFixed64(fieldNumber, value.toLong())

    actual fun writeFixed64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeFixed64SizeNoTag(it.toLong()) },
            ::writeFixed64NoTag,
            ::writeFixed64
        )

    }

    actual fun writeFixed64NoTag(value: ULong) = impl.writeFixed64NoTag(value.toLong())

    actual fun writeFloat(fieldNumber: Int, value: Float) = impl.writeFloat(fieldNumber, value)

    actual fun writeFloatArray(
        fieldNumber: Int,
        values: List<Float>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeFloatSizeNoTag,
            ::writeFloatNoTag,
            ::writeFloat
        )
    }

    actual fun writeFloatNoTag(value: Float) = impl.writeFloatNoTag(value)

    actual fun writeGroup(
        fieldNumber: Int,
        value: KMMessage
    ): Unit = TODO()

    actual fun writeGroupArray(
        fieldNumber: Int,
        values: List<KMMessage>
    ): Unit = TODO()

    actual fun writeGroupNoTag(
        fieldNumber: Int,
        value: KMMessage
    ): Unit = TODO()

    actual fun writeInt32(fieldNumber: Int, value: Int) = impl.writeInt32(fieldNumber, value)

    actual fun writeInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeInt32SizeNoTag,
            ::writeInt32NoTag,
            ::writeInt32
        )
    }

    actual fun writeInt32NoTag(value: Int) = impl.writeInt32NoTag(value)

    actual fun writeInt64(fieldNumber: Int, value: Long) = impl.writeInt64(fieldNumber, value)

    actual fun writeInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeInt64SizeNoTag,
            ::writeInt64NoTag,
            ::writeInt64
        )
    }

    actual fun writeInt64NoTag(value: Long) = impl.writeInt64NoTag(value)

    actual fun writeMessage(
        fieldNumber: Int,
        value: KMMessage
    ) {
        writeKMMessage(
            this,
            fieldNumber,
            value,
            value.requiredSize.toUInt(),
            serializeMessage
        )
    }

    actual fun writeMessageArray(
        fieldNumber: Int,
        values: List<KMMessage>
    ) {
        writeMessageList(
            this,
            fieldNumber,
            values,
            requiredSizeMessage,
            serializeMessage
        )
    }

    actual fun writeMessageNoTag(value: KMMessage) {
        serializeMessage(value, this)
    }

    actual fun writeMessageSetExtension(
        fieldNumber: Int,
        value: KMMessage
    ): Unit = TODO()

    actual fun writeRawByte(value: UByte) = impl.writeRawByte(value.toByte())

    actual fun writeRawData(data: ByteArray) = impl.writeRawBytes(ByteString.copyFrom(data))

    actual fun writeRawLittleEndian32(value: Int) = impl.writeFixed32NoTag(value)

    actual fun writeRawLittleEndian64(value: Long) = impl.writeFixed64NoTag(value)

    actual fun writeRawMessageSetExtension(fieldNumber: Int, value: ByteArray): Unit = TODO()

    actual fun writeRawVarint32(value: Int) = impl.writeUInt32NoTag(value)

    actual fun writeRawVarint64(value: Long) = impl.writeUInt64NoTag(value)

    actual fun writeRawVarintSizeTAs32(value: ULong): Unit = throw NotImplementedError("Not supported on JVM")

    actual fun writeSFixed32(fieldNumber: Int, value: Int) = impl.writeSFixed32(fieldNumber, value)

    actual fun writeSFixed32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSFixed32SizeNoTag,
            ::writeSFixed32NoTag,
            ::writeSFixed32
        )
    }

    actual fun writeSFixed32NoTag(value: Int) = impl.writeSFixed32NoTag(value)

    actual fun writeSFixed64(fieldNumber: Int, value: Long) = impl.writeSFixed64(fieldNumber, value)

    actual fun writeSFixed64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSFixed64SizeNoTag,
            ::writeSFixed64NoTag,
            ::writeSFixed64
        )
    }

    actual fun writeSFixed64NoTag(value: Long) = impl.writeSFixed64NoTag(value)

    actual fun writeSInt32(fieldNumber: Int, value: Int) = impl.writeSInt32(fieldNumber, value)

    actual fun writeSInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSInt32SizeNoTag,
            ::writeSInt32NoTag,
            ::writeSInt32
        )
    }

    actual fun writeSInt32NoTag(value: Int) = impl.writeSInt32NoTag(value)

    actual fun writeSInt64(fieldNumber: Int, value: Long) = impl.writeSInt64(fieldNumber, value)

    actual fun writeSInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            CodedOutputStream::computeSInt64SizeNoTag,
            ::writeSInt64NoTag,
            ::writeSInt64
        )
    }

    actual fun writeSInt64NoTag(value: Long) = impl.writeSInt64NoTag(value)

    actual fun writeString(fieldNumber: Int, value: String) = impl.writeString(fieldNumber, value)
    actual fun writeStringArray(fieldNumber: Int, values: List<String>) {
        writeArray(
            this,
            fieldNumber,
            values,
            0u,
            CodedOutputStream::computeStringSizeNoTag,
            ::writeStringNoTag,
            ::writeString
        )
    }

    actual fun writeStringNoTag(value: String) = impl.writeStringNoTag(value)

    actual fun writeTag(
        fieldNumber: Int,
        format: WireFormat
    ) {
        //https://github.com/protocolbuffers/protobuf/blob/main/objectivec/GPBCodedOutputStream.m#L120
        impl.writeRawVarint32(wireFormatMakeTag(fieldNumber, format))
    }

    actual fun writeUInt32(fieldNumber: Int, value: UInt) = impl.writeUInt32(fieldNumber, value.toInt())

    actual fun writeUInt32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeUInt32SizeNoTag(it.toInt()) },
            ::writeUInt32NoTag,
            ::writeUInt32
        )
    }

    actual fun writeUInt32NoTag(value: UInt) = impl.writeUInt32NoTag(value.toInt())

    actual fun writeUInt64(fieldNumber: Int, value: ULong) = impl.writeUInt64(fieldNumber, value.toLong())

    actual fun writeUInt64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) {
        writeArray(
            this,
            fieldNumber,
            values,
            tag,
            { CodedOutputStream.computeUInt64SizeNoTag(it.toLong()) },
            ::writeUInt64NoTag,
            ::writeUInt64
        )

    }

    actual fun writeUInt64NoTag(value: ULong) = impl.writeUInt64NoTag(value.toLong())
}