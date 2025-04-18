@file:JvmName("ActualSizeComputation")

package io.github.timortel.kmpgrpc.core.io

import com.google.protobuf.CodedOutputStream
import io.github.timortel.kmpgrpc.core.message.Message

actual fun computeTagSize(fieldNumber: Int): Int = CodedOutputStream.computeTagSize(fieldNumber)

actual fun computeRawVarint32Size(value: Int): Int = CodedOutputStream.computeUInt32SizeNoTag(value)


actual fun computeDoubleSize(fieldNumber: Int, value: Double): Int =
    CodedOutputStream.computeDoubleSize(fieldNumber, value)

actual fun computeDoubleSizeNoTag(value: Double): Int =
    CodedOutputStream.computeDoubleSizeNoTag(value)

actual fun computeFloatSize(fieldNumber: Int, value: Float): Int =
    CodedOutputStream.computeFloatSize(fieldNumber, value)

actual fun computeFloatSizeNoTag(value: Float): Int =
    CodedOutputStream.computeFloatSizeNoTag(value)

actual fun computeInt32Size(fieldNumber: Int, value: Int): Int =
    CodedOutputStream.computeInt32Size(fieldNumber, value)

actual fun computeInt32SizeNoTag(value: Int): Int =
    CodedOutputStream.computeInt32SizeNoTag(value)

actual fun computeInt64Size(fieldNumber: Int, value: Long): Int =
    CodedOutputStream.computeInt64Size(fieldNumber, value)

actual fun computeInt64SizeNoTag(value: Long): Int =
    CodedOutputStream.computeInt64SizeNoTag(value)

actual fun computeUInt32Size(fieldNumber: Int, value: UInt): Int =
    CodedOutputStream.computeUInt32Size(fieldNumber, value.toInt())

actual fun computeUInt32SizeNoTag(value: UInt): Int =
    CodedOutputStream.computeUInt32SizeNoTag(value.toInt())

actual fun computeUInt64Size(fieldNumber: Int, value: ULong): Int =
    CodedOutputStream.computeUInt64Size(fieldNumber, value.toLong())

actual fun computeUInt64SizeNoTag(value: ULong): Int =
    CodedOutputStream.computeUInt64SizeNoTag(value.toLong())

actual fun computeSInt32Size(fieldNumber: Int, value: Int): Int =
    CodedOutputStream.computeSInt32Size(fieldNumber, value)

actual fun computeSInt32SizeNoTag(value: Int): Int =
    CodedOutputStream.computeSInt32SizeNoTag(value)

actual fun computeSInt64Size(fieldNumber: Int, value: Long): Int =
    CodedOutputStream.computeSInt64Size(fieldNumber, value)

actual fun computeSInt64SizeNoTag(value: Long): Int =
    CodedOutputStream.computeSInt64SizeNoTag(value)

actual fun computeFixed32Size(fieldNumber: Int, value: UInt): Int =
    CodedOutputStream.computeFixed32Size(fieldNumber, value.toInt())

actual fun computeFixed32SizeNoTag(value: UInt): Int =
    CodedOutputStream.computeFixed32SizeNoTag(value.toInt())

actual fun computeFixed64Size(fieldNumber: Int, value: ULong): Int =
    CodedOutputStream.computeFixed64Size(fieldNumber, value.toLong())

actual fun computeFixed64SizeNoTag(value: ULong): Int =
    CodedOutputStream.computeFixed64SizeNoTag(value.toLong())

actual fun computeSFixed32Size(fieldNumber: Int, value: Int): Int =
    CodedOutputStream.computeSFixed32Size(fieldNumber, value)

actual fun computeSFixed32SizeNoTag(value: Int): Int =
    CodedOutputStream.computeSFixed32SizeNoTag(value)

actual fun computeSFixed64Size(fieldNumber: Int, value: Long): Int =
    CodedOutputStream.computeSFixed64Size(fieldNumber, value)

actual fun computeSFixed64SizeNoTag(value: Long): Int =
    CodedOutputStream.computeSFixed64SizeNoTag(value)

actual fun computeBoolSize(fieldNumber: Int, value: Boolean): Int =
    CodedOutputStream.computeBoolSize(fieldNumber, value)

actual fun computeBoolSizeNoTag(value: Boolean): Int =
    CodedOutputStream.computeBoolSizeNoTag(value)

actual fun computeStringSize(fieldNumber: Int, value: String): Int =
    CodedOutputStream.computeStringSize(fieldNumber, value)

actual fun computeStringSizeNoTag(value: String): Int =
    CodedOutputStream.computeStringSizeNoTag(value)

actual fun computeBytesSize(fieldNumber: Int, value: ByteArray): Int =
    CodedOutputStream.computeTagSize(fieldNumber) + computeBytesSizeNoTag(value)

actual fun computeBytesSizeNoTag(value: ByteArray): Int =
    CodedOutputStream.computeUInt32SizeNoTag(value.size) + value.size

actual fun computeEnumSize(fieldNumber: Int, value: Int): Int =
    CodedOutputStream.computeEnumSize(fieldNumber, value)

actual fun computeEnumSizeNoTag(value: Int): Int =
    CodedOutputStream.computeEnumSizeNoTag(value)

actual fun computeMessageSize(fieldNumber: Int, value: Message?): Int = if (value != null) {
    computeTagSize(fieldNumber) + computeMessageSizeNoTag(value)
} else 0

actual fun computeMessageSizeNoTag(value: Message?): Int =
    if (value != null) CodedOutputStream.computeUInt32SizeNoTag(
        value.requiredSize
    ) + value.requiredSize else 0