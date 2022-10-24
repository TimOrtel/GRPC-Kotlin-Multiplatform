package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

actual fun computeTagSize(fieldNumber: Int): Int = GPBComputeTagSize(fieldNumber).toInt()

actual fun computeRawVarint32Size(value: Int): Int = GPBComputeRawVarint32Size(value).toInt()

actual fun computeDoubleSize(fieldNumber: Int, value: Double): Int = GPBComputeDoubleSize(fieldNumber, value).toInt()
actual fun computeDoubleSizeNoTag(value: Double): Int = GPBComputeDoubleSizeNoTag(value).toInt()

actual fun computeFloatSize(fieldNumber: Int, value: Float): Int = GPBComputeFloatSize(fieldNumber, value).toInt()
actual fun computeFloatSizeNoTag(value: Float): Int = GPBComputeFloatSizeNoTag(value).toInt()

actual fun computeInt32Size(fieldNumber: Int, value: Int): Int = GPBComputeInt32Size(fieldNumber, value).toInt()
actual fun computeInt32SizeNoTag(value: Int): Int = GPBComputeInt32SizeNoTag(value).toInt()

actual fun computeInt64Size(fieldNumber: Int, value: Long): Int = GPBComputeInt64Size(fieldNumber, value).toInt()
actual fun computeInt64SizeNoTag(value: Long): Int = GPBComputeInt64SizeNoTag(value).toInt()

actual fun computeBoolSize(fieldNumber: Int, value: Boolean): Int = GPBComputeBoolSize(fieldNumber, value).toInt()
actual fun computeBoolSizeNoTag(value: Boolean): Int = GPBComputeBoolSizeNoTag(value).toInt()

actual fun computeStringSize(fieldNumber: Int, value: String): Int = GPBComputeStringSize(fieldNumber, value).toInt()
actual fun computeStringSizeNoTag(value: String): Int = GPBComputeStringSizeNoTag(value).toInt()

actual fun computeEnumSize(fieldNumber: Int, value: Int): Int = GPBComputeEnumSize(fieldNumber, value).toInt()
actual fun computeEnumSizeNoTag(value: Int): Int = GPBComputeEnumSizeNoTag(value).toInt()

actual fun computeMessageSize(fieldNumber: Int, value: KMMessage?): Int =
    if (value != null) {
        computeTagSize(fieldNumber) + computeMessageSizeNoTag(value)
    } else 0

actual fun computeMessageSizeNoTag(value: KMMessage?): Int = if (value != null) GPBComputeRawVarint32Size(
    value.requiredSize
).toInt() + value.requiredSize else 0