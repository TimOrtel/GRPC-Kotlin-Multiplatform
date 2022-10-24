package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import cocoapods.Protobuf.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.io.kMapKeyFieldNumber
import io.github.timortel.kotlin_multiplatform_grpc_lib.io.kMapValueFieldNumber
import platform.darwin.NSInteger
import platform.posix.size_t

fun computeMessageSize(fieldNumber: Int, msg: KMMessage?): size_t =
    if (msg != null) {
        GPBComputeTagSize(fieldNumber) + computeMessageSizeNoTag(msg)
    } else 0u

fun computeMessageSizeNoTag(msg: KMMessage?): size_t = if (msg != null) GPBComputeRawVarint32SizeForInteger(
    msg.requiredSize.toLong()
) + msg.requiredSize else 0u

fun <K, V> computeMapSize(fieldNumber: Int, map: Map<K, V>, calculateKeySize: (fieldNumber: Int, K) -> ULong, calculateValueSize: (fieldNumber: Int, V) -> ULong): size_t {
    val mapSize = map.entries.sumOf { (key, value) ->
        val msgSize: size_t = calculateKeySize(kMapKeyFieldNumber, key) + calculateValueSize(kMapValueFieldNumber, value)
        GPBComputeRawVarint32Size(msgSize.toInt()) + msgSize
    }

    //https://github.com/protocolbuffers/protobuf/blob/520c601c99012101c816b6ccc89e8d6fc28fdbb8/objectivec/GPBDictionary.m#L343
    //GPBDataTypeMessage is used in the original source
    val tagSize = GPBComputeWireFormatTagSize(fieldNumber, GPBDataTypeMessage)
    return mapSize + tagSize * map.size.toUInt()
}