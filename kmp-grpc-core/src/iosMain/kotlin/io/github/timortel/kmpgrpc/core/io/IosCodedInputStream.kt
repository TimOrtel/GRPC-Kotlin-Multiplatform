package io.github.timortel.kmpgrpc.core.io

import cocoapods.Protobuf.GPBCodedInputStream
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

/**
 * Implements the functionality by wrapping [GPBCodedInputStream].
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosCodedInputStream(private val impl: GPBCodedInputStream) :
    CodedInputStream() {

    override val isAtEnd: Boolean
        get() = impl.isAtEnd()

    override fun readTag(): Int = impl.readTag()

    @Throws(ParseException::class)
    override fun checkLastTagWas(value: Int) {
        try {
            impl.checkLastTagWas(value)
        } catch (_: Exception) {
            throw ParseException()
        }
    }

    override fun skipField(tag: Int): Boolean = impl.skipField(tag)

    override fun readDouble(): Double = impl.readDouble()

    override fun readFloat(): Float = impl.readFloat()

    override fun readUInt64(): ULong = impl.readUInt64()

    override fun readInt64(): Long = impl.readInt64()

    override fun readInt32(): Int = impl.readInt32()

    override fun readFixed32(): UInt = impl.readFixed32()

    override fun readFixed64(): ULong = impl.readFixed64()

    override fun readBool(): Boolean = impl.readBool()

    override fun readString(): String = impl.readString()

    override fun readBytes(): ByteArray {
        val data = impl.readBytes()
        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }

    override fun readByteArray(): ByteArray = readBytes()

    override fun readUInt32(): UInt = impl.readUInt32()

    override fun readEnum(): Int = impl.readEnum()

    override fun readSFixed32(): Int = impl.readSFixed32()

    override fun readSFixed64(): Long = impl.readSFixed64()

    override fun readSInt32(): Int = impl.readSInt32()

    override fun readSInt64(): Long = impl.readSInt64()

    override fun readRawVarint32(): Int = impl.readInt32()

    override fun readRawVarint64(): Long = impl.readInt64()

    override fun readRawByte(): Byte = throw NotImplementedError("Not available on ios")

    override fun pushLimit(newLimit: Int): Int = impl.pushLimit(newLimit.toULong()).toInt()

    override fun popLimit(oldLimit: Int) = impl.popLimit(oldLimit.toULong())

    override fun setSizeLimit(newLimit: Int): Int = throw NotImplementedError()
}
