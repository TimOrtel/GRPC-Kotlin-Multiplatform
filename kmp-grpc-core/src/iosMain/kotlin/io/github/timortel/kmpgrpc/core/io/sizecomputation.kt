package io.github.timortel.kmpgrpc.core.io

import cocoapods.Protobuf.*
import io.github.timortel.kmpgrpc.core.message.Message

actual fun computeTagSize(fieldNumber: Int): Int = GPBComputeTagSize(fieldNumber).toInt()

actual fun computeRawVarint32Size(value: Int): Int = GPBComputeRawVarint32Size(value).toInt()

actual fun computeDoubleSize(fieldNumber: Int, value: Double): Int =
    GPBComputeDoubleSize(fieldNumber, value).toInt()

actual fun computeDoubleSizeNoTag(value: Double): Int =
    GPBComputeDoubleSizeNoTag(value).toInt()

actual fun computeFloatSize(fieldNumber: Int, value: Float): Int =
    GPBComputeFloatSize(fieldNumber, value).toInt()

actual fun computeFloatSizeNoTag(value: Float): Int =
    GPBComputeFloatSizeNoTag(value).toInt()

actual fun computeInt32Size(fieldNumber: Int, value: Int): Int =
    GPBComputeInt32Size(fieldNumber, value).toInt()

actual fun computeInt32SizeNoTag(value: Int): Int =
    GPBComputeInt32SizeNoTag(value).toInt()

actual fun computeInt64Size(fieldNumber: Int, value: Long): Int =
    GPBComputeInt64Size(fieldNumber, value).toInt()

actual fun computeInt64SizeNoTag(value: Long): Int =
    GPBComputeInt64SizeNoTag(value).toInt()

actual fun computeUInt32Size(fieldNumber: Int, value: UInt): Int =
    GPBComputeUInt32Size(fieldNumber, value).toInt()

actual fun computeUInt32SizeNoTag(value: UInt): Int =
    GPBComputeUInt32SizeNoTag(value.toInt()).toInt()

actual fun computeUInt64Size(fieldNumber: Int, value: ULong): Int =
    GPBComputeUInt64Size(fieldNumber, value).toInt()

actual fun computeUInt64SizeNoTag(value: ULong): Int =
    GPBComputeUInt64SizeNoTag(value).toInt()

actual fun computeSInt32Size(fieldNumber: Int, value: Int): Int =
    GPBComputeSInt32Size(fieldNumber, value).toInt()

actual fun computeSInt32SizeNoTag(value: Int): Int =
    GPBComputeSInt32SizeNoTag(value).toInt()

actual fun computeSInt64Size(fieldNumber: Int, value: Long): Int =
    GPBComputeSInt64Size(fieldNumber, value).toInt()

actual fun computeSInt64SizeNoTag(value: Long): Int =
    GPBComputeSInt64SizeNoTag(value).toInt()

actual fun computeFixed32Size(fieldNumber: Int, value: UInt): Int =
    GPBComputeFixed32Size(fieldNumber, value).toInt()

actual fun computeFixed32SizeNoTag(value: UInt): Int =
    GPBComputeFixed32SizeNoTag(value).toInt()

actual fun computeFixed64Size(fieldNumber: Int, value: ULong): Int =
    GPBComputeFixed64Size(fieldNumber, value).toInt()

actual fun computeFixed64SizeNoTag(value: ULong): Int =
    GPBComputeFixed64SizeNoTag(value).toInt()

actual fun computeSFixed32Size(fieldNumber: Int, value: Int): Int =
    GPBComputeSFixed32Size(fieldNumber, value).toInt()

actual fun computeSFixed32SizeNoTag(value: Int): Int =
    GPBComputeSFixed32SizeNoTag(value).toInt()

actual fun computeSFixed64Size(fieldNumber: Int, value: Long): Int =
    GPBComputeSFixed64Size(fieldNumber, value).toInt()

actual fun computeSFixed64SizeNoTag(value: Long): Int =
    GPBComputeSFixed64SizeNoTag(value).toInt()

actual fun computeBoolSize(fieldNumber: Int, value: Boolean): Int =
    GPBComputeBoolSize(fieldNumber, value).toInt()

actual fun computeBoolSizeNoTag(value: Boolean): Int =
    GPBComputeBoolSizeNoTag(value).toInt()

actual fun computeStringSize(fieldNumber: Int, value: String): Int =
    GPBComputeStringSize(fieldNumber, value).toInt()

actual fun computeStringSizeNoTag(value: String): Int =
    GPBComputeStringSizeNoTag(value).toInt()

actual fun computeBytesSize(fieldNumber: Int, value: ByteArray): Int =
    computeTagSize(fieldNumber) + computeBytesSizeNoTag(value)

actual fun computeBytesSizeNoTag(value: ByteArray): Int =
    computeUInt32SizeNoTag(value.size.toUInt()) + value.size

actual fun computeEnumSize(fieldNumber: Int, value: Int): Int = GPBComputeEnumSize(fieldNumber, value).toInt()
actual fun computeEnumSizeNoTag(value: Int): Int = GPBComputeEnumSizeNoTag(value).toInt()

actual fun computeMessageSize(fieldNumber: Int, value: Message?): Int =
    if (value != null) {
        computeTagSize(fieldNumber) + computeMessageSizeNoTag(value)
    } else 0

actual fun computeMessageSizeNoTag(value: Message?): Int = if (value != null) GPBComputeRawVarint32Size(
    value.requiredSize
).toInt() + value.requiredSize else 0