package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.DataType
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

/**
 * Base class that decodes messages sent over the network connection. Counterpart to [CodedOutputStream].
 * See [the java CodedInputStream implementation](https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/CodedInputStream.java) for further details.
 */
expect class CodedInputStream {

    var recursionDepth: Int

    val bytesUntilLimit: Int

    val isAtEnd: Boolean

    fun readTag(): Int

    @Throws(ParseException::class)
    fun checkLastTagWas(value: Int)

    fun getLastTag(): Int

    fun skipField(tag: Int): Boolean

    fun skipMessage()

    fun readDouble(): Double

    fun readFloat(): Float

    fun readUInt64(): ULong

    fun readInt64(): Long

    fun readInt32(): Int

    fun readFixed32(): UInt

    fun readFixed64(): ULong

    fun readBool(): Boolean

    fun readString(): String

    /**
     * Read a [KMMessage] from the stream.
     */
    fun <M : KMMessage> readKMMessage(messageFactory: (CodedInputStream) -> M): M

    /**
     * Read a map entry from the stream following the grpc specification.
     */
    fun <K, V> readMapEntry(
        map: MutableMap<K, V>,
        keyDataType: DataType,
        valueDataType: DataType,
        defaultKey: K?,
        defaultValue: V?,
        readKey: CodedInputStream.() -> K,
        readValue: CodedInputStream.() -> V
    )

    fun readBytes(): ByteArray

    fun readByteArray(): ByteArray

    fun readUInt32(): UInt

    fun readEnum(): Int

    fun readSFixed32(): Int

    fun readSFixed64(): Long

    fun readSInt32(): Int

    fun readSInt64(): Long

    fun readRawVarint32(): Int

    fun readRawVarint64(): Long

    fun readRawByte(): Byte

    fun pushLimit(newLimit: Int): Int

    fun popLimit(oldLimit: Int)

    fun setSizeLimit(newLimit: Int): Int
}