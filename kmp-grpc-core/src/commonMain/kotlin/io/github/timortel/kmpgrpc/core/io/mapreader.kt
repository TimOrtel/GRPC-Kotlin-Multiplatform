package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi
import io.github.timortel.kmpgrpc.shared.internal.io.DataType
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatForType
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatMakeTag

// Adapted version of https://github.com/protocolbuffers/protobuf/blob/520c601c99012101c816b6ccc89e8d6fc28fdbb8/objectivec/GPBDictionary.m#L455
/**
 * Reads a map entry from a coded input stream and adds it to the given mutable map.
 *
 * @param map The mutable map to which the read key-value pair should be added.
 * @param keyDataType The data type of the key in the map.
 * @param valueDataType The data type of the value in the map.
 * @param defaultKey The default key to use if no key is read.
 * @param defaultValue The default value to use if no value is read.
 */
@InternalKmpGrpcApi
fun <K, V> readMapEntry(
    stream: CodedInputStream,
    fieldNumber: Int,
    map: MutableMap<K, V>,
    unknownFields: MutableList<UnknownField>,
    keyDataType: DataType,
    valueDataType: DataType,
    defaultKey: K?,
    defaultValue: V?,
    readKey: CodedInputStream.() -> K,
    readValue: CodedInputStream.() -> V?
) {
    stream.recursiveRead { length ->
        val mapEntry = stream.peek(length)

        val keyTag = wireFormatMakeTag(kMapKeyFieldNumber, wireFormatForType(keyDataType, false))
        val valueTag =
            wireFormatMakeTag(kMapValueFieldNumber, wireFormatForType(valueDataType, false))

        var key: K? = defaultKey
        var value: V? = defaultValue

        var hitError = false

        while (true) {
            when (val tag = stream.readTag()) {
                0 -> break
                keyTag -> key = stream.readKey()
                valueTag -> value = stream.readValue()
                else -> {
                    //Unknown
                    if (!stream.skipField(tag)) {
                        hitError = true
                        break
                    }
                }
            }
        }

        if (!hitError && key != null && value != null) {
            map[key] = value
        } else {
            unknownFields += UnknownField.LengthDelimited(fieldNumber, mapEntry)
        }
    }
}
