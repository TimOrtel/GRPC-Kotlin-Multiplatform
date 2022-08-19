package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.kMapKeyFieldNumber
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.kMapValueFieldNumber
import platform.posix.int32_t
import platform.posix.uint32_t

fun <M : KMMessage> readKMMessage(
    wrapper: GPBCodedInputStreamWrapper,
    messageFactory: (GPBCodedInputStreamWrapper) -> M
): M = recursiveRead(wrapper) { messageFactory(wrapper) }


/*
 Adapted version of https://github.com/protocolbuffers/protobuf/blob/520c601c99012101c816b6ccc89e8d6fc28fdbb8/objectivec/GPBDictionary.m#L455
 */
fun <K, V> readMapEntry(
    wrapper: GPBCodedInputStreamWrapper,
    map: MutableMap<K, V>,
    keyDataType: GPBDataType,
    valueDataType: GPBDataType,
    defaultKey: K?,
    defaultValue: V?,
    readKey: GPBCodedInputStreamWrapper.() -> K,
    readValue: GPBCodedInputStreamWrapper.() -> V
) {
    recursiveRead(wrapper) {
        val keyTag = GPBWireFormatMakeTag(kMapKeyFieldNumber.toUInt(), GPBWireFormatForType(keyDataType, false)).toInt()
        val valueTag =
            GPBWireFormatMakeTag(kMapValueFieldNumber.toUInt(), GPBWireFormatForType(valueDataType, false)).toInt()

        var key: K? = defaultKey
        var value: V? = defaultValue

        var hitError = false

        while (true) {
            when (val tag = wrapper.stream.readTag()) {
                0 -> break
                keyTag -> key = wrapper.readKey()
                valueTag -> value = wrapper.readValue()
                else -> {
                    //Unknown
                    if (!wrapper.stream.skipField(tag)) {
                        hitError = true
                        break
                    }
                }
            }
        }


        if (!hitError && key != null && value != null) {
            map[key] = value
        }
    }
}

private fun <T> recursiveRead(wrapper: GPBCodedInputStreamWrapper, block: () -> T): T {
    checkRecursionLimit(wrapper)
    val length: int32_t = wrapper.stream.readInt32()
    val oldLimit = wrapper.stream.pushLimit(length.toULong())
    wrapper.recursionDepth++
    val r = block()
    wrapper.stream.checkLastTagWas(0)
    wrapper.recursionDepth--
    wrapper.stream.popLimit(oldLimit)
    return r
}

private fun checkRecursionLimit(wrapper: GPBCodedInputStreamWrapper) {
    if (wrapper.recursionDepth >= 100) {
        throw RuntimeException("Recursion depth exceeded.")
    }
}