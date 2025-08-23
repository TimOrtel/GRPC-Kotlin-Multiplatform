package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.shared.DataType
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.shared.WireFormat
import io.github.timortel.kmpgrpc.shared.wireFormatForType
import io.github.timortel.kmpgrpc.shared.wireFormatGetTagFieldNumber
import io.github.timortel.kmpgrpc.shared.wireFormatGetTagWireType
import io.github.timortel.kmpgrpc.shared.wireFormatMakeTag

/**
 * Base class that decodes messages sent over the network connection. Counterpart to [CodedOutputStream].
 * See [the java CodedInputStream implementation](https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/CodedInputStream.java) for further details.
 *
 * This class is not stable for inheritance.
 */
abstract class CodedInputStream {

    companion object {
        private const val DEFAULT_RECURSION_LIMIT = 100
    }

    var recursionDepth: Int = 0

    abstract val isAtEnd: Boolean

    abstract fun readTag(): Int

    @Throws(ParseException::class)
    abstract fun checkLastTagWas(value: Int)

    abstract fun skipField(tag: Int): Boolean

    abstract fun readDouble(): Double

    abstract fun readFloat(): Float

    abstract fun readUInt64(): ULong

    abstract fun readInt64(): Long

    abstract fun readInt32(): Int

    abstract fun readFixed32(): UInt

    abstract fun readFixed64(): ULong

    abstract fun readBool(): Boolean

    abstract fun readString(): String

    /**
     * Reads and deserializes a gRPC message from the input stream using the provided deserializer.
     *
     * @param deserializer The implementation of [MessageDeserializer] used to transform the serialized message
     *                     data into an instance of type [M].
     * @return An instance of type [M], which extends [Message], deserialized from the input stream.
     */
    fun <M : Message> readMessage(deserializer: MessageDeserializer<M>): M {
        return recursiveRead { deserializer.deserialize(this) }
    }

    // Adapted version of https://github.com/protocolbuffers/protobuf/blob/520c601c99012101c816b6ccc89e8d6fc28fdbb8/objectivec/GPBDictionary.m#L455
    /**
     * Reads a map entry from a coded input stream and adds it to the given mutable map.
     *
     * @param map The mutable map to which the read key-value pair should be added.
     * @param keyDataType The data type of the key in the map.
     * @param valueDataType The data type of the value in the map.
     * @param defaultKey The default key to use if no key is read.
     * @param defaultValue The default value to use if no value is read.
     * @param readKey A lambda function defining how to read the key from the input stream.
     * @param readValue A lambda function defining how to read the value from the input stream.
     */
    fun <K, V> readMapEntry(
        map: MutableMap<K, V>,
        keyDataType: DataType,
        valueDataType: DataType,
        defaultKey: K?,
        defaultValue: V?,
        readKey: CodedInputStream.() -> K,
        readValue: CodedInputStream.() -> V
    ) {
        recursiveRead {
            val keyTag = wireFormatMakeTag(kMapKeyFieldNumber, wireFormatForType(keyDataType, false))
            val valueTag =
                wireFormatMakeTag(kMapValueFieldNumber, wireFormatForType(valueDataType, false))

            var key: K? = defaultKey
            var value: V? = defaultValue

            var hitError = false

            while (true) {
                when (val tag = readTag()) {
                    0 -> break
                    keyTag -> key = readKey()
                    valueTag -> value = readValue()
                    else -> {
                        //Unknown
                        if (!skipField(tag)) {
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

    abstract fun readBytes(): ByteArray

    abstract fun readUInt32(): UInt

    abstract fun readEnum(): Int

    abstract fun readSFixed32(): Int

    abstract fun readSFixed64(): Long

    abstract fun readSInt32(): Int

    abstract fun readSInt64(): Long

    abstract fun pushLimit(newLimit: Int): Int

    abstract fun popLimit(oldLimit: Int)

    fun readUnknownField(tag: Int): UnknownField? {
        val number = wireFormatGetTagFieldNumber(tag)
        val wireTypeNumber = wireFormatGetTagWireType(tag)

        return when (WireFormat.entries.firstOrNull { it.value == wireTypeNumber }) {
            WireFormat.VARINT -> UnknownField.Varint(number, readInt64())
            WireFormat.FIXED32 -> UnknownField.Fixed32(number, readFixed32())
            WireFormat.FIXED64 -> UnknownField.Fixed64(number, readFixed64())
            WireFormat.LENGTH_DELIMITED -> UnknownField.LengthDelimited(number, readBytes())
            WireFormat.START_GROUP -> readGroup(number)
            WireFormat.END_GROUP -> null
            null -> throw ParseException("Could not decipher wire format type")
        }
    }

    private fun readGroup(number: Int): UnknownField.Group {
        checkRecursionLimit()
        recursionDepth++

        val fields = buildList {
            while (true) {
                val tag = readTag()
                if (wireFormatGetTagWireType(tag) == WireFormat.END_GROUP.value || tag == 0) {
                    break
                }

                val unknownField = readUnknownField(tag)
                if (unknownField != null) {
                    add(unknownField)
                }
            }
        }

        recursionDepth--
        checkLastTagWas(wireFormatMakeTag(number, WireFormat.END_GROUP))

        return UnknownField.Group(number, fields)
    }

    private fun <T> recursiveRead(readEntry: () -> T): T {
        checkRecursionLimit()
        val length: Int = readInt32()
        val oldLimit = pushLimit(length)
        recursionDepth++
        val readResult = readEntry()
        checkLastTagWas(0)
        recursionDepth--
        popLimit(oldLimit)
        return readResult
    }

    private fun checkRecursionLimit() {
        if (recursionDepth >= DEFAULT_RECURSION_LIMIT) {
            throw RuntimeException("Recursion depth exceeded.")
        }
    }
}
