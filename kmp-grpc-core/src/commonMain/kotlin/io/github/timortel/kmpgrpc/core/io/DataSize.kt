// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

/*
 * This file is an adapted translation of: https://github.com/protocolbuffers/protobuf/blob/bf34ebee2801d089083977b087a2f6e65f3c7022/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java
 * Took the logic of computing field sizes
 * Adapted:
 * - Translated to Kotlin
 * - Extracted only the necessary size computation fields
 * - Put into DataSize object.
 */

package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.io.internal.DataEncoding
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.shared.WireFormat
import io.github.timortel.kmpgrpc.shared.wireFormatMakeTag
import okio.utf8Size

object DataSize {

    private const val FIXED32_SIZE: Int = 4
    private const val FIXED64_SIZE: Int = 8
    private const val MAX_VARINT_SIZE: Int = 10

    fun computeInt32Size(fieldNumber: Int, value: Int): Int {
        return computeTagSize(fieldNumber) + computeInt32SizeNoTag(value)
    }

    fun computeUInt32Size(fieldNumber: Int, value: UInt): Int {
        return computeTagSize(fieldNumber) + computeUInt32SizeNoTag(value)
    }

    fun computeSInt32Size(fieldNumber: Int, value: Int): Int {
        return computeTagSize(fieldNumber) + computeSInt32SizeNoTag(value)
    }

    fun computeFixed32Size(fieldNumber: Int, value: UInt): Int {
        return computeTagSize(fieldNumber) + computeFixed32SizeNoTag(value)
    }

    fun computeSFixed32Size(fieldNumber: Int, value: Int): Int {
        return computeTagSize(fieldNumber) + computeSFixed32SizeNoTag(value)
    }

    fun computeInt64Size(fieldNumber: Int, value: Long): Int {
        return computeTagSize(fieldNumber) + computeInt64SizeNoTag(value)
    }

    fun computeUInt64Size(fieldNumber: Int, value: ULong): Int {
        return computeTagSize(fieldNumber) + computeUInt64SizeNoTag(value)
    }

    fun computeSInt64Size(fieldNumber: Int, value: Long): Int {
        return computeTagSize(fieldNumber) + computeSInt64SizeNoTag(value)
    }

    fun computeFixed64Size(fieldNumber: Int, value: ULong): Int {
        return computeTagSize(fieldNumber) + computeFixed64SizeNoTag(value)
    }

    fun computeSFixed64Size(fieldNumber: Int, value: Long): Int {
        return computeTagSize(fieldNumber) + computeSFixed64SizeNoTag(value)
    }

    fun computeFloatSize(fieldNumber: Int, value: Float): Int {
        return computeTagSize(fieldNumber) + computeFloatSizeNoTag(value)
    }

    fun computeDoubleSize(fieldNumber: Int, value: Double): Int {
        return computeTagSize(fieldNumber) + computeDoubleSizeNoTag(value)
    }

    fun computeBoolSize(fieldNumber: Int, value: Boolean): Int {
        return computeTagSize(fieldNumber) + computeBoolSizeNoTag(value)
    }

    fun computeEnumSize(fieldNumber: Int, value: Int): Int {
        return computeTagSize(fieldNumber) + computeEnumSizeNoTag(value)
    }

    fun computeStringSize(fieldNumber: Int, value: String): Int {
        return computeTagSize(fieldNumber) + computeStringSizeNoTag(value)
    }

    fun computeByteArraySize(fieldNumber: Int, value: ByteArray): Int {
        return computeTagSize(fieldNumber) + computeByteArraySizeNoTag(value)
    }

    fun computeByteArraySizeNoTag(value: ByteArray): Int {
        return computeLengthDelimitedFieldSize(value.size)
    }

    fun computeLengthDelimitedFieldSize(fieldLength: Int): Int {
        return computeUInt32SizeNoTag(fieldLength.toUInt()) + fieldLength
    }

    fun computeTagSize(fieldNumber: Int): Int {
        return computeUInt32SizeNoTag(wireFormatMakeTag(fieldNumber, WireFormat.VARINT).toUInt())
    }

    fun computeInt32SizeNoTag(value: Int): Int {
        return if (value >= 0) {
            computeUInt32SizeNoTag(value.toUInt())
        } else {
            // Must sign-extend.
            MAX_VARINT_SIZE
        }
    }


    fun computeUInt32SizeNoTag(value: UInt): Int {
        if ((value and (0u.inv() shl 7)) == 0u) {
            return 1
        }
        if ((value and (0u.inv() shl 14)) == 0u) {
            return 2
        }
        if ((value and (0u.inv() shl 21)) == 0u) {
            return 3
        }
        if ((value and (0u.inv() shl 28)) == 0u) {
            return 4
        }
        return 5
    }

    fun computeSInt32SizeNoTag(value: Int): Int {
        return computeUInt32SizeNoTag(DataEncoding.encodeZigZag32(value))
    }

    fun computeFixed32SizeNoTag(@Suppress("unused") unused: UInt): Int {
        return FIXED32_SIZE
    }

    fun computeSFixed32SizeNoTag(@Suppress("unused") unused: Int): Int {
        return FIXED32_SIZE
    }

    fun computeInt64SizeNoTag(value: Long): Int {
        return if (value >= 0) {
            computeUInt64SizeNoTag(value.toULong())
        } else {
            MAX_VARINT_SIZE
        }
    }

    fun computeUInt64SizeNoTag(value: ULong): Int {
        var value1 = value
        if ((value1 and (0uL.inv() shl 7)) == 0uL) {
            return 1
        }
        if (value1 < 0uL) {
            return 10
        }

        var n = 2
        if ((value1 and (0uL.inv() shl 35)) != 0uL) {
            n += 4
            value1 = value1 shr 28
        }
        if ((value1 and (0uL.inv() shl 21)) != 0uL) {
            n += 2
            value1 = value1 shr 14
        }
        if ((value1 and (0uL.inv() shl 14)) != 0uL) {
            n += 1
        }
        return n
    }


    fun computeSInt64SizeNoTag(value: Long): Int {
        return computeUInt64SizeNoTag(DataEncoding.encodeZigZag64(value))
    }

    fun computeFixed64SizeNoTag(@Suppress("unused") unused: ULong): Int {
        return FIXED64_SIZE
    }

    fun computeSFixed64SizeNoTag(@Suppress("unused") unused: Long): Int {
        return FIXED64_SIZE
    }

    fun computeFloatSizeNoTag(@Suppress("unused") unused: Float): Int {
        return FIXED32_SIZE
    }

    fun computeDoubleSizeNoTag(@Suppress("unused") unused: Double): Int {
        return FIXED64_SIZE
    }

    fun computeBoolSizeNoTag(@Suppress("unused") unused: Boolean): Int {
        return 1
    }

    fun computeEnumSizeNoTag(value: Int): Int {
        return computeInt32SizeNoTag(value)
    }

    fun computeStringSizeNoTag(value: String): Int {
        return computeLengthDelimitedFieldSize(value.utf8Size().toInt())
    }

    fun computeMessageSize(fieldNumber: Int, value: Message?): Int {
        return if (value != null) computeTagSize(fieldNumber) + computeMessageSizeNoTag(value)
        else 0
    }

    fun computeMessageSizeNoTag(value: Message?): Int {
        return if (value == null) 0 else computeLengthDelimitedFieldSize(value.requiredSize)
    }

    fun <K, V> computeMapSize(
        fieldNumber: Int,
        map: Map<K, V>,
        calculateKeySize: (fieldNumber: Int, K) -> Int,
        calculateValueSize: (fieldNumber: Int, V) -> Int
    ): Int {
        val mapSize = map.entries.sumOf { (key, value) ->
            val msgSize =
                calculateKeySize(kMapKeyFieldNumber, key) + calculateValueSize(kMapValueFieldNumber, value)
            computeInt32SizeNoTag(msgSize) + msgSize
        }

        val tagSize = computeUInt32SizeNoTag(wireFormatMakeTag(fieldNumber, WireFormat.LENGTH_DELIMITED).toUInt())
        return mapSize + tagSize * map.size
    }

    fun computeUnknownFieldsRequiredSize(fields: List<UnknownField>): Int {
        return fields.sumOf { field ->
            val contentSize = when (field) {
                is UnknownField.Varint -> computeInt64SizeNoTag(field.value)
                is UnknownField.LengthDelimited -> computeByteArraySizeNoTag(field.value)
                is UnknownField.Fixed32 -> computeFixed32SizeNoTag(field.value)
                is UnknownField.Fixed64 -> computeFixed64SizeNoTag(field.value)
                // A group has an additional end tag
                is UnknownField.Group -> computeUnknownFieldsRequiredSize(field.values) + computeTagSize(
                    field.number
                )
            }

            computeTagSize(field.number) + contentSize
        }
    }
}
