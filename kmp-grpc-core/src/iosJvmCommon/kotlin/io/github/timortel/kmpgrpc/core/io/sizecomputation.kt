package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.DataType
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.UnknownField

fun computeUnknownFieldsRequiredSize(fields: List<UnknownField>): Int {
    return fields.sumOf { field ->
        val contentSize = when (field) {
            is UnknownField.Varint -> computeInt64SizeNoTag(field.value)
            is UnknownField.LengthDelimited -> computeBytesSizeNoTag(field.value)
            is UnknownField.Fixed32 -> computeFixed32SizeNoTag(field.value)
            is UnknownField.Fixed64 -> computeFixed64SizeNoTag(field.value)
            // A group has an additional end tag
            is UnknownField.Group -> computeUnknownFieldsRequiredSize(field.values) + computeTagSize(field.number)
        }

        computeTagSize(field.number) + contentSize
    }
}

expect fun computeTagSize(fieldNumber: Int): Int

fun computeWireFormatTagSize(fieldNumber: Int, dataType: DataType): Int {
    //https://github.com/protocolbuffers/protobuf/blob/main/objectivec/GPBCodedOutputStream.m#L1076
    val result = computeTagSize(fieldNumber)
    return if (dataType == DataType.GROUP) result * 2 else result
}

expect fun computeRawVarint32Size(value: Int): Int

expect fun computeDoubleSize(fieldNumber: Int, value: Double): Int
expect fun computeDoubleSizeNoTag(value: Double): Int

expect fun computeFloatSize(fieldNumber: Int, value: Float): Int
expect fun computeFloatSizeNoTag(value: Float): Int

expect fun computeInt32Size(fieldNumber: Int, value: Int): Int
expect fun computeInt32SizeNoTag(value: Int): Int

expect fun computeInt64Size(fieldNumber: Int, value: Long): Int
expect fun computeInt64SizeNoTag(value: Long): Int

expect fun computeBoolSize(fieldNumber: Int, value: Boolean): Int
expect fun computeBoolSizeNoTag(value: Boolean): Int

expect fun computeStringSize(fieldNumber: Int, value: String): Int
expect fun computeStringSizeNoTag(value: String): Int

expect fun computeEnumSize(fieldNumber: Int, value: Int): Int
expect fun computeEnumSizeNoTag(value: Int): Int

expect fun computeUInt32Size(fieldNumber: Int, value: UInt): Int
expect fun computeUInt32SizeNoTag(value: UInt): Int

expect fun computeUInt64Size(fieldNumber: Int, value: ULong): Int
expect fun computeUInt64SizeNoTag(value: ULong): Int

expect fun computeSInt32Size(fieldNumber: Int, value: Int): Int
expect fun computeSInt32SizeNoTag(value: Int): Int

expect fun computeSInt64Size(fieldNumber: Int, value: Long): Int
expect fun computeSInt64SizeNoTag(value: Long): Int

expect fun computeFixed32Size(fieldNumber: Int, value: UInt): Int
expect fun computeFixed32SizeNoTag(value: UInt): Int

expect fun computeFixed64Size(fieldNumber: Int, value: ULong): Int
expect fun computeFixed64SizeNoTag(value: ULong): Int

expect fun computeSFixed32Size(fieldNumber: Int, value: Int): Int
expect fun computeSFixed32SizeNoTag(value: Int): Int

expect fun computeSFixed64Size(fieldNumber: Int, value: Long): Int
expect fun computeSFixed64SizeNoTag(value: Long): Int

expect fun computeBytesSize(fieldNumber: Int, value: ByteArray): Int
expect fun computeBytesSizeNoTag(value: ByteArray): Int

expect fun computeMessageSize(fieldNumber: Int, value: Message?): Int
expect fun computeMessageSizeNoTag(value: Message?): Int



fun <K, V> computeMapSize(
    fieldNumber: Int,
    map: Map<K, V>,
    calculateKeySize: (fieldNumber: Int, K) -> Int,
    calculateValueSize: (fieldNumber: Int, V) -> Int
): Int {
    val mapSize = map.entries.sumOf { (key, value) ->
        val msgSize =
            calculateKeySize(kMapKeyFieldNumber, key) + calculateValueSize(kMapValueFieldNumber, value)
        computeRawVarint32Size(msgSize) + msgSize
    }

    //https://github.com/protocolbuffers/protobuf/blob/520c601c99012101c816b6ccc89e8d6fc28fdbb8/objectivec/GPBDictionary.m#L343
    //GPBDataTypeMessage is used in the original source
    val tagSize = computeWireFormatTagSize(fieldNumber, DataType.MESSAGE)
    return mapSize + tagSize * map.size
}
