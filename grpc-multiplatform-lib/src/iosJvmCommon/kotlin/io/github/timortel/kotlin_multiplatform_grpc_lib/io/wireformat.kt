package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.DataType

/**
 * From: https://github.com/protocolbuffers/protobuf/blob/fcd3b9a85ef36e46643dc30176cea1a7ad62e02b/objectivec/GPBWireFormat.m
 */

private const val TYPE_BITS = 3
private const val TYPE_MASK = 7

fun wireFormatMakeTag(fieldNumber: Int, wireType: WireFormat): Int = (fieldNumber shl TYPE_BITS) or wireType.value

fun wireFormatGetTagWireType(tag: Int): Int = tag and TYPE_MASK

fun wireFormatGetTagFieldNumber(tag: Int): Int = tag ushr TYPE_BITS

fun wireFormatIsValidTag(tag: Int): Boolean {
    val formatBits = tag and TYPE_MASK
    return formatBits <= 5
}

fun wireFormatForType(dataType: DataType, isPacked: Boolean): WireFormat {
    if (isPacked) return WireFormat.LENGTH_DELIMITED

    return when (dataType) {
        DataType.BOOL, DataType.INT32, DataType.INT64, DataType.SINT32, DataType.SINT64, DataType.UINT32, DataType.UINT64, DataType.ENUM -> WireFormat.VARINT
        DataType.FIXED32, DataType.SFIXED32, DataType.FLOAT -> WireFormat.FIXED32
        DataType.FIXED64, DataType.SFIXED64, DataType.DOUBLE -> WireFormat.FIXED64
        DataType.BYTES, DataType.STRING, DataType.MESSAGE -> WireFormat.LENGTH_DELIMITED
        DataType.GROUP -> WireFormat.START_GROUP
        else -> throw IllegalArgumentException("DataType $dataType unknown.")
    }
}

enum class WireFormat(val value: Int) {
    VARINT(0),
    FIXED64(1),
    LENGTH_DELIMITED(2),
    START_GROUP(3),
    END_GROUP(4),
    FIXED32(5)
}