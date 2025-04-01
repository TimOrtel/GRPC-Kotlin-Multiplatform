package io.github.timortel.kmpgrpc.core.io

import cocoapods.Protobuf.GPBCodedInputStream
import io.github.timortel.kmpgrpc.core.message.DataType
import io.github.timortel.kmpgrpc.core.message.KMMessage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

/**
 * Implements the functionality by wrapping [GPBCodedInputStream].
 */
@OptIn(ExperimentalForeignApi::class)
actual class CodedInputStream(private val impl: GPBCodedInputStream, actual var recursionDepth: Int = 0) {

    actual val bytesUntilLimit: Int get() = throw NotImplementedError()
    actual val isAtEnd: Boolean
        get() = impl.isAtEnd()

    actual fun readTag(): Int = impl.readTag()

    @Throws(ParseException::class)
    actual fun checkLastTagWas(value: Int) {
        try {
            impl.checkLastTagWas(value)
        } catch (_: Exception) {
            throw ParseException()
        }
    }

    actual fun getLastTag(): Int = throw NotImplementedError("Not available on iOS")

    actual fun skipField(tag: Int): Boolean = impl.skipField(tag)

    actual fun skipMessage() = impl.skipMessage()

    actual fun readDouble(): Double = impl.readDouble()

    actual fun readFloat(): Float = impl.readFloat()

    actual fun readUInt64(): ULong = impl.readUInt64()

    actual fun readInt64(): Long = impl.readInt64()

    actual fun readInt32(): Int = impl.readInt32()

    actual fun readFixed32(): UInt = impl.readFixed32()

    actual fun readFixed64(): ULong = impl.readFixed64()

    actual fun readBool(): Boolean = impl.readBool()

    actual fun readString(): String = impl.readString()

    actual fun <M : KMMessage> readKMMessage(messageFactory: (CodedInputStream) -> M): M =
        io.github.timortel.kmpgrpc.core.io.readKMMessage(this, messageFactory)

    actual fun <K, V> readMapEntry(
        map: MutableMap<K, V>,
        keyDataType: DataType,
        valueDataType: DataType,
        defaultKey: K?,
        defaultValue: V?,
        readKey: CodedInputStream.() -> K,
        readValue: CodedInputStream.() -> V
    ) = io.github.timortel.kmpgrpc.core.io.readMapEntry(
        this,
        map,
        keyDataType,
        valueDataType,
        defaultKey,
        defaultValue,
        readKey,
        readValue
    )

    actual fun readBytes(): ByteArray {
        val data = impl.readBytes()
        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }

    actual fun readByteArray(): ByteArray = readBytes()

    actual fun readUInt32(): UInt = impl.readUInt32()

    actual fun readEnum(): Int = impl.readEnum()

    actual fun readSFixed32(): Int = impl.readSFixed32()

    actual fun readSFixed64(): Long = impl.readSFixed64()

    actual fun readSInt32(): Int = impl.readSInt32()

    actual fun readSInt64(): Long = impl.readSInt64()

    actual fun readRawVarint32(): Int = impl.readInt32()

    actual fun readRawVarint64(): Long = impl.readInt64()

    actual fun readRawByte(): Byte = throw NotImplementedError("Not available on ios")

    actual fun pushLimit(newLimit: Int): Int = impl.pushLimit(newLimit.toULong()).toInt()

    actual fun popLimit(oldLimit: Int) = impl.popLimit(oldLimit.toULong())

    actual fun setSizeLimit(newLimit: Int): Int = throw NotImplementedError()
}