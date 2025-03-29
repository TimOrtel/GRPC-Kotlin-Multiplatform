package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KmEnum

/**
 * Base class that encodes messages to send them over the network connection. Counterpart to [CodedInputStream].
 * See [the java CodedOutputStream implementation](https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java) for further details.
 */
expect class CodedOutputStream {
    fun writeBool(fieldNumber: Int, value: Boolean)

    fun writeBoolArray(fieldNumber: Int, value: List<Boolean>, tag: UInt)

    fun writeBoolNoTag(value: Boolean)

    fun writeBytes(fieldNumber: Int, value: ByteArray)

    fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>,)

    fun writeBytesNoTag(value: ByteArray)

    fun writeDouble(fieldNumber: Int, value: Double)

    fun writeDoubleArray(fieldNumber: Int, values: List<Double>, tag: UInt)

    fun writeDoubleNoTag(value: Double)

    fun writeEnum(fieldNumber: Int, value: Int)

    fun writeEnum(fieldNumber: Int, value: KmEnum)

    fun writeEnumArray(fieldNumber: Int, values: List<KmEnum>, tag: UInt)

    fun writeEnumNoTag(value: Int)

    fun writeFixed32(fieldNumber: Int, value: UInt)

    fun writeFixed32Array(fieldNumber: Int, values: List<UInt>, tag: UInt)

    fun writeFixed32NoTag(value: UInt)

    fun writeFixed64(fieldNumber: Int, value: ULong)

    fun writeFixed64Array(fieldNumber: Int, values: List<ULong>, tag: UInt)

    fun writeFixed64NoTag(value: ULong)

    fun writeFloat(fieldNumber: Int, value: Float)

    fun writeFloatArray(fieldNumber: Int, values: List<Float>, tag: UInt)

    fun writeFloatNoTag(value: Float)

    fun writeGroup(fieldNumber: Int, value: KMMessage)

    fun writeGroupArray(fieldNumber: Int, values: List<KMMessage>)

    fun writeGroupNoTag(fieldNumber: Int, value: KMMessage)

    fun writeInt32(fieldNumber: Int, value: Int)

    fun writeInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeInt32NoTag(value: Int)

    fun writeInt64(fieldNumber: Int, value: Long)

    fun writeInt64Array(fieldNumber: Int, values: List<Long>, tag: UInt)

    fun writeInt64NoTag(value: Long)

    fun writeMessage(fieldNumber: Int, value: KMMessage)

    fun writeMessageArray(fieldNumber: Int, values: List<KMMessage>)

    fun writeMessageNoTag(value: KMMessage)

    fun writeMessageSetExtension(fieldNumber: Int, value: KMMessage)

    fun writeRawByte(value: UByte)

    fun writeRawData(data: ByteArray)

    fun writeRawLittleEndian32(value: Int)

    fun writeRawLittleEndian64(value: Long)

    fun writeRawMessageSetExtension(fieldNumber: Int, value: ByteArray)

    fun writeRawVarint32(value: Int)

    fun writeRawVarint64(value: Long)

    fun writeRawVarintSizeTAs32(value: ULong)

    fun writeSFixed32(fieldNumber: Int, value: Int)

    fun writeSFixed32Array(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeSFixed32NoTag(value: Int)

    fun writeSFixed64(fieldNumber: Int, value: Long)

    fun writeSFixed64Array(fieldNumber: Int, values: List<Long>, tag: UInt)

    fun writeSFixed64NoTag(value: Long)

    fun writeSInt32(fieldNumber: Int, value: Int)

    fun writeSInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeSInt32NoTag(value: Int)

    fun writeSInt64(fieldNumber: Int, value: Long)

    fun writeSInt64Array(fieldNumber: Int, values: List<Long>, tag: UInt)

    fun writeSInt64NoTag(value: Long)

    fun writeString(fieldNumber: Int, value: String)

    fun writeStringArray(fieldNumber: Int, values: List<String>)

    fun writeStringNoTag(value: String)

    fun writeTag(fieldNumber: UInt, format: WireFormat)

    fun writeUInt32(fieldNumber: Int, value: UInt)

    fun writeUInt32Array(fieldNumber: Int, values: List<UInt>, tag: UInt)

    fun writeUInt32NoTag(value: UInt)

    fun writeUInt64(fieldNumber: Int, value: ULong)

    fun writeUInt64Array(fieldNumber: Int, values: List<ULong>, tag: UInt)

    fun writeUInt64NoTag(value: ULong)

//      fun writeUnknownGroup(fieldNumber: Int, value: cocoapods.Protobuf.GPBUnknownFieldSet)
//
//      fun writeUnknownGroupArray(fieldNumber: Int, values: List<*>)
//
//      fun writeUnknownGroupNoTag(fieldNumber: Int, value: cocoapods.Protobuf.GPBUnknownFieldSet)
}