package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import com.google.protobuf.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.DataType
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

actual fun computeTagSize(fieldNumber: Int): Int = CodedOutputStream.computeTagSize(fieldNumber)

actual fun computeRawVarint32Size(value: Int): Int = CodedOutputStream.computeUInt32SizeNoTag(value)


actual fun computeDoubleSize(fieldNumber: Int, value: Double): Int =
    CodedOutputStream.computeDoubleSize(fieldNumber, value)

actual fun computeDoubleSizeNoTag(value: Double): Int = CodedOutputStream.computeDoubleSizeNoTag(value)

actual fun computeFloatSize(fieldNumber: Int, value: Float): Int =
    CodedOutputStream.computeFloatSize(fieldNumber, value)

actual fun computeFloatSizeNoTag(value: Float): Int = CodedOutputStream.computeFloatSizeNoTag(value)

actual fun computeInt32Size(fieldNumber: Int, value: Int): Int = CodedOutputStream.computeInt32Size(fieldNumber, value)
actual fun computeInt32SizeNoTag(value: Int): Int = CodedOutputStream.computeInt32SizeNoTag(value)

actual fun computeInt64Size(fieldNumber: Int, value: Long): Int = CodedOutputStream.computeInt64Size(fieldNumber, value)
actual fun computeInt64SizeNoTag(value: Long): Int = CodedOutputStream.computeInt64SizeNoTag(value)

actual fun computeBoolSize(fieldNumber: Int, value: Boolean): Int =
    CodedOutputStream.computeBoolSize(fieldNumber, value)

actual fun computeBoolSizeNoTag(value: Boolean): Int = CodedOutputStream.computeBoolSizeNoTag(value)

actual fun computeStringSize(fieldNumber: Int, value: String): Int =
    CodedOutputStream.computeStringSize(fieldNumber, value)

actual fun computeStringSizeNoTag(value: String): Int = CodedOutputStream.computeStringSizeNoTag(value)

actual fun computeEnumSize(fieldNumber: Int, value: Int): Int = CodedOutputStream.computeEnumSize(fieldNumber, value)
actual fun computeEnumSizeNoTag(value: Int): Int = CodedOutputStream.computeEnumSizeNoTag(value)

actual fun computeMessageSize(fieldNumber: Int, value: KMMessage?): Int = if (value != null) {
    computeTagSize(fieldNumber) + computeMessageSizeNoTag(value)
} else 0

actual fun computeMessageSizeNoTag(value: KMMessage?): Int =
    if (value != null) CodedOutputStream.computeUInt32SizeNoTag(
        value.requiredSize
    ) + value.requiredSize else 0