// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

/*
 * This file is an adapted translation of: https://github.com/protocolbuffers/protobuf/blob/bf662dca814c7535b75d1520f9f32a76f7205c02/objectivec/GPBWireFormat.m
 * Changes:
 * - Translated to Kotlin
 * - Took only the necessary functions and functionality
 */

package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.DataType

/**
 * From: https://github.com/protocolbuffers/protobuf/blob/fcd3b9a85ef36e46643dc30176cea1a7ad62e02b/objectivec/GPBWireFormat.m
 */

private const val TYPE_BITS = 3
private const val TYPE_MASK = 7

fun wireFormatMakeTag(fieldNumber: Int, dataType: DataType, isPacked: Boolean): Int = wireFormatMakeTag(
    fieldNumber = fieldNumber,
    wireType = wireFormatForType(dataType, isPacked)
)

fun wireFormatMakeTag(fieldNumber: Int, wireType: WireFormat): Int = (fieldNumber shl TYPE_BITS) or wireType.value

fun wireFormatGetTagWireType(tag: Int): Int = tag and TYPE_MASK

fun wireFormatGetTagFieldNumber(tag: Int): Int = tag ushr TYPE_BITS

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

fun getWireFormatByValue(value: Int): WireFormat? {
    return when (value) {
        0 -> WireFormat.VARINT
        1 -> WireFormat.FIXED64
        2 -> WireFormat.LENGTH_DELIMITED
        3 -> WireFormat.START_GROUP
        4 -> WireFormat.END_GROUP
        5 -> WireFormat.FIXED32
        else -> null
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
