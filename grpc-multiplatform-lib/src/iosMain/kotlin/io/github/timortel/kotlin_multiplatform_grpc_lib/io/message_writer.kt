package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

fun writeKMMessage(stream: GPBCodedOutputStream, fieldNumber: Int, msg: KMMessage) {
    stream.writeUInt32NoTag(GPBWireFormatMakeTag(fieldNumber.toUInt(), GPBWireFormatLengthDelimited))
    stream.writeUInt32NoTag(msg.requiredSize.toUInt())

    msg.serialize(stream)
}

private fun <T> writeList(
    stream: GPBCodedOutputStream,
    fieldNumber: Int,
    values: List<T>,
    tag: UInt,
    computeDataSizeNoTag: (T) -> ULong,
    writeNoTag: GPBCodedOutputStream.(T) -> Unit,
    writeWithTag: GPBCodedOutputStream.(fieldNumber: Int, T) -> Unit
) {
    if (tag != 0u) {
        //Write packed
        if (values.isEmpty()) return

        val dataSize: UInt = values.sumOf(computeDataSizeNoTag).toUInt()
        stream.writeUInt32NoTag(tag)
        stream.writeUInt32NoTag(dataSize)

        values.forEach { writeNoTag(stream, it) }
    } else {
        //Write unpacked
        values.forEach { writeWithTag(stream, fieldNumber, it) }
    }
}

fun writeBoolList(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<Boolean>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeBoolSizeNoTag,
        writeNoTag = { writeBoolNoTag(it) },
        writeWithTag = { fn, v -> writeBool(fn, v) }
    )
}

fun writeInt32List(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<Int>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeInt32SizeNoTag,
        writeNoTag = { writeInt32NoTag(it) },
        writeWithTag = { fn, v -> writeInt32(fn, v) }
    )
}

fun writeInt64List(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<Long>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeInt64SizeNoTag,
        writeNoTag = { writeInt64NoTag(it) },
        writeWithTag = { fn, v -> writeInt64(fn, v) }
    )
}

fun writeStringList(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<String>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeStringSizeNoTag,
        writeNoTag = { writeStringNoTag(it) },
        writeWithTag = { fn, v -> writeString(fn, v) }
    )
}

fun writeDoubleList(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<Double>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeDoubleSizeNoTag,
        writeNoTag = { writeDoubleNoTag(it) },
        writeWithTag = { fn, v -> writeDouble(fn, v) }
    )
}

fun writeFloatList(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<Float>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeFloatSizeNoTag,
        writeNoTag = { writeFloatNoTag(it) },
        writeWithTag = { fn, v -> writeFloat(fn, v) }
    )
}

fun writeEnumList(stream: GPBCodedOutputStream, fieldNumber: Int, values: List<Int>, tag: UInt) {
    writeList(
        stream = stream,
        fieldNumber = fieldNumber,
        values = values,
        tag = tag,
        computeDataSizeNoTag = ::GPBComputeEnumSizeNoTag,
        writeNoTag = { writeEnumNoTag(it) },
        writeWithTag = { fn, v -> writeEnum(fn, v) }
    )
}