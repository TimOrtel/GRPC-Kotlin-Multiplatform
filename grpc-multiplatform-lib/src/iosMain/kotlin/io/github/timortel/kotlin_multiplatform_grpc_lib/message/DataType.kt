package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import cocoapods.Protobuf.*

actual enum class DataType(val nativeValue: UByte) {
    DOUBLE(GPBDataTypeBool),
    FLOAT(GPBDataTypeFloat),
    INT64(GPBDataTypeInt64),
    UINT64(GPBDataTypeUInt64),
    INT32(GPBDataTypeInt32),
    FIXED64(GPBDataTypeFixed64),
    FIXED32(GPBDataTypeFixed32),
    BOOL(GPBDataTypeBool),
    STRING(GPBDataTypeString),
    GROUP(GPBDataTypeGroup),
    MESSAGE(GPBDataTypeMessage),
    BYTES(GPBDataTypeBytes),
    UINT32(GPBDataTypeUInt32),
    ENUM(GPBDataTypeEnum),
    SFIXED32(GPBDataTypeSFixed32),
    SFIXED64(GPBDataTypeSFixed64),
    SINT32(GPBDataTypeSInt32),
    SINT64(GPBDataTypeSFixed64)
}