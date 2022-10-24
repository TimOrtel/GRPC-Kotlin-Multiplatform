package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.DataType
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

fun <M : KMMessage> readKMMessage(
    wrapper: CodedInputStream,
    messageFactory: (CodedInputStream) -> M
): M = recursiveRead(wrapper) { messageFactory(wrapper) }

/*
 Adapted version of https://github.com/protocolbuffers/protobuf/blob/520c601c99012101c816b6ccc89e8d6fc28fdbb8/objectivec/GPBDictionary.m#L455
 */
fun <K, V> readMapEntry(
    inputStream: CodedInputStream,
    map: MutableMap<K, V>,
    keyDataType: DataType,
    valueDataType: DataType,
    defaultKey: K?,
    defaultValue: V?,
    readKey: CodedInputStream.() -> K,
    readValue: CodedInputStream.() -> V
) {
    recursiveRead(inputStream) {
        val keyTag = wireFormatMakeTag(kMapKeyFieldNumber, wireFormatForType(keyDataType, false))
        val valueTag =
            wireFormatMakeTag(kMapValueFieldNumber, wireFormatForType(valueDataType, false))

        var key: K? = defaultKey
        var value: V? = defaultValue

        var hitError = false

        while (true) {
            when (val tag = inputStream.readTag()) {
                0 -> break
                keyTag -> key = inputStream.readKey()
                valueTag -> value = inputStream.readValue()
                else -> {
                    //Unknown
                    if (!inputStream.skipField(tag)) {
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

private fun <T> recursiveRead(stream: CodedInputStream, block: () -> T): T {
    checkRecursionLimit(stream)
    val length: Int = stream.readInt32()
    val oldLimit = stream.pushLimit(length)
    stream.recursionDepth++
    val r = block()
    stream.checkLastTagWas(0)
    stream.recursionDepth--
    stream.popLimit(oldLimit)
    return r

    //checkRecursionLimit(wrapper)
    //val length: int32_t = wrapper.stream.readInt32()
    //val oldLimit = wrapper.stream.pushLimit(length.toULong())
    //wrapper.recursionDepth++
    //val r = block()
    //wrapper.stream.checkLastTagWas(0)
    //wrapper.recursionDepth--
    //wrapper.stream.popLimit(oldLimit)
    //return r
}

private fun checkRecursionLimit(wrapper: CodedInputStream) {
    if (wrapper.recursionDepth >= 100) {
        throw RuntimeException("Recursion depth exceeded.")
    }
}