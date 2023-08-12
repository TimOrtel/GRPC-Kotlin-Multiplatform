package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.requiredSizeMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.serializeMessage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * Implements the functionality by wrapping [GPBCodedOutputStream].
 */
actual class CodedOutputStream(private val impl: GPBCodedOutputStream) {

    @OptIn(ExperimentalForeignApi::class)
    companion object {
        private val ByteArray.native: NSData get() {
            return memScoped {
                NSData.create(bytes = allocArrayOf(this@native), length = this@native.size.toULong())
            }
        }
    }

    actual fun writeBool(fieldNumber: Int, value: Boolean) = impl.writeBool(fieldNumber, value)

    actual fun writeBoolArray(fieldNumber: Int, value: List<Boolean>, tag: UInt): Unit =
        writeArray(this, fieldNumber, value, tag, ::GPBComputeBoolSizeNoTag, ::writeBoolNoTag, ::writeBool)

    actual fun writeBoolNoTag(value: Boolean) = impl.writeBoolNoTag(value)

    actual fun writeBytes(fieldNumber: Int, value: ByteArray) {
        impl.writeBytes(fieldNumber, value.native)
    }

    actual fun writeBytesArray(fieldNumber: Int, value: List<ByteArray>, tag: UInt) {
        val computeBytesSizeNoTag = { bytes: ByteArray ->
            GPBComputeRawVarint32Size(bytes.size) + bytes.size.toULong()
        }

        writeArray(this, fieldNumber, value, tag, computeBytesSizeNoTag, ::writeBytesNoTag, ::writeBytes)
    }

    actual fun writeBytesNoTag(value: ByteArray) {
        impl.writeBytesNoTag(value.native)
    }

    actual fun writeDouble(fieldNumber: Int, value: Double) = impl.writeDouble(fieldNumber, value)

    actual fun writeDoubleArray(
        fieldNumber: Int,
        values: List<Double>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeDoubleSizeNoTag, ::writeDoubleNoTag, ::writeDouble)
    }

    actual fun writeDoubleNoTag(value: Double) = impl.writeDoubleNoTag(value)

    actual fun writeEnum(fieldNumber: Int, value: Int) = impl.writeEnum(fieldNumber, value)

    actual fun writeEnumArray(fieldNumber: Int, values: List<Int>, tag: UInt) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeEnumSizeNoTag, ::writeEnumNoTag, ::writeEnum)
    }

    actual fun writeEnumNoTag(value: Int) = impl.writeEnumNoTag(value)

    actual fun writeFixed32(fieldNumber: Int, value: UInt) = impl.writeFixed32(fieldNumber, value)

    actual fun writeFixed32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeFixed32SizeNoTag, ::writeFixed32NoTag, ::writeFixed32)
    }

    actual fun writeFixed32NoTag(value: UInt) = impl.writeFixed32NoTag(value)

    actual fun writeFixed64(fieldNumber: Int, value: ULong) = impl.writeFixed64(fieldNumber, value)

    actual fun writeFixed64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeFixed64SizeNoTag, ::writeFixed64NoTag, ::writeFixed64)

    }

    actual fun writeFixed64NoTag(value: ULong) = impl.writeFixed64NoTag(value)

    actual fun writeFloat(fieldNumber: Int, value: Float) = impl.writeFloat(fieldNumber, value)

    actual fun writeFloatArray(
        fieldNumber: Int,
        values: List<Float>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeFloatSizeNoTag, ::writeFloatNoTag, ::writeFloat)
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
        writeArray(this, fieldNumber, values, tag, ::GPBComputeInt32SizeNoTag, ::writeInt32NoTag, ::writeInt32)
    }

    actual fun writeInt32NoTag(value: Int) = impl.writeInt32NoTag(value)

    actual fun writeInt64(fieldNumber: Int, value: Long) = impl.writeInt64(fieldNumber, value)

    actual fun writeInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeInt64SizeNoTag, ::writeInt64NoTag, ::writeInt64)
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

    actual fun writeRawByte(value: UByte) = impl.writeRawByte(value)

    actual fun writeRawData(data: ByteArray) = impl.writeRawData(data.native)

    actual fun writeRawLittleEndian32(value: Int) = impl.writeRawLittleEndian32(value)

    actual fun writeRawLittleEndian64(value: Long) = impl.writeRawLittleEndian64(value)

    actual fun writeRawMessageSetExtension(fieldNumber: Int, value: ByteArray): Unit = TODO()

    actual fun writeRawVarint32(value: Int) = impl.writeRawVarint32(value)

    actual fun writeRawVarint64(value: Long) = impl.writeRawVarint64(value)

    actual fun writeRawVarintSizeTAs32(value: ULong) = impl.writeRawVarintSizeTAs32(value)

    actual fun writeSFixed32(fieldNumber: Int, value: Int) = impl.writeSFixed32(fieldNumber, value)

    actual fun writeSFixed32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeSFixed32SizeNoTag, ::writeSFixed32NoTag, ::writeSFixed32)
    }

    actual fun writeSFixed32NoTag(value: Int) = impl.writeSFixed32NoTag(value)

    actual fun writeSFixed64(fieldNumber: Int, value: Long) = impl.writeSFixed64(fieldNumber, value)

    actual fun writeSFixed64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeSFixed64SizeNoTag, ::writeSFixed64NoTag, ::writeSFixed64)
    }

    actual fun writeSFixed64NoTag(value: Long) = impl.writeSFixed64NoTag(value)

    actual fun writeSInt32(fieldNumber: Int, value: Int) = impl.writeSInt32(fieldNumber, value)

    actual fun writeSInt32Array(
        fieldNumber: Int,
        values: List<Int>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeSInt32SizeNoTag, ::writeSInt32NoTag, ::writeSInt32)
    }

    actual fun writeSInt32NoTag(value: Int) = impl.writeSInt32NoTag(value)

    actual fun writeSInt64(fieldNumber: Int, value: Long) = impl.writeSInt64(fieldNumber, value)

    actual fun writeSInt64Array(
        fieldNumber: Int,
        values: List<Long>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeSInt64SizeNoTag, ::writeSInt64NoTag, ::writeSInt64)
    }

    actual fun writeSInt64NoTag(value: Long) = impl.writeSInt64NoTag(value)

    actual fun writeString(fieldNumber: Int, value: String) = impl.writeString(fieldNumber, value)
    actual fun writeStringArray(fieldNumber: Int, values: List<String>) {
        writeArray(this, fieldNumber, values, 0u, ::GPBComputeStringSizeNoTag, ::writeStringNoTag, ::writeString)
    }

    actual fun writeStringNoTag(value: String) = impl.writeStringNoTag(value)

    actual fun writeTag(
        fieldNumber: UInt,
        format: WireFormat
    ) {
        //https://github.com/protocolbuffers/protobuf/blob/main/objectivec/GPBCodedOutputStream.m#L120
        impl.writeRawVarint32(wireFormatMakeTag(fieldNumber.toInt(), format))
    }

    actual fun writeUInt32(fieldNumber: Int, value: UInt) = impl.writeUInt32(fieldNumber, value)

    actual fun writeUInt32Array(
        fieldNumber: Int,
        values: List<UInt>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, { GPBComputeUInt32SizeNoTag(it.toInt()) }, ::writeUInt32NoTag, ::writeUInt32)
    }

    actual fun writeUInt32NoTag(value: UInt) = impl.writeUInt32NoTag(value)

    actual fun writeUInt64(fieldNumber: Int, value: ULong) = impl.writeUInt64(fieldNumber, value)

    actual fun writeUInt64Array(
        fieldNumber: Int,
        values: List<ULong>,
        tag: UInt
    ) {
        writeArray(this, fieldNumber, values, tag, ::GPBComputeUInt64SizeNoTag, ::writeUInt64NoTag, ::writeUInt64)

    }

    actual fun writeUInt64NoTag(value: ULong) = impl.writeUInt64NoTag(value)
}