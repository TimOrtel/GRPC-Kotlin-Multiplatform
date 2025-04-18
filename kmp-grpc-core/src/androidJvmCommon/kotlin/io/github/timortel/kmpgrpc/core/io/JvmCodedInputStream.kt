package io.github.timortel.kmpgrpc.core.io

import com.google.protobuf.CodedInputStream
import com.google.protobuf.InvalidProtocolBufferException
import kotlin.jvm.Throws

internal class JvmCodedInputStream(
    private val impl: CodedInputStream,
) : io.github.timortel.kmpgrpc.core.io.CodedInputStream() {

    override val isAtEnd: Boolean
        get() = impl.isAtEnd

    override fun readTag(): Int = impl.readTag()

    override fun skipField(tag: Int): Boolean = impl.skipField(tag)

    override fun readDouble(): Double = impl.readDouble()

    override fun readFloat(): Float = impl.readFloat()

    override fun readUInt64(): ULong = impl.readUInt64().toULong()

    override fun readInt64(): Long = impl.readInt64()

    override fun readInt32(): Int = impl.readInt32()

    override fun readFixed32(): UInt = impl.readFixed32().toUInt()

    override fun readFixed64(): ULong = impl.readFixed64().toULong()

    override fun readBool(): Boolean = impl.readBool()

    override fun readString(): String = impl.readString()

    override fun readBytes(): ByteArray = impl.readBytes().toByteArray()

    override fun readByteArray(): ByteArray = impl.readByteArray()

    override fun readUInt32(): UInt = impl.readUInt32().toUInt()

    override fun readEnum(): Int = impl.readEnum()

    override fun readSFixed32(): Int = impl.readSFixed32()

    override fun readSFixed64(): Long = impl.readSFixed64()

    override fun readSInt32(): Int = impl.readSInt32()

    override fun readSInt64(): Long = impl.readSInt64()

    override fun readRawVarint32(): Int = impl.readRawVarint32()

    override fun readRawVarint64(): Long = impl.readRawVarint64()

    override fun readRawByte(): Byte = impl.readRawByte()

    override fun pushLimit(newLimit: Int): Int = impl.pushLimit(newLimit)

    override fun popLimit(oldLimit: Int) = impl.popLimit(oldLimit)

    @Throws(ParseException::class)
    override fun checkLastTagWas(value: Int) {
        try {
            impl.checkLastTagWas(value)
        } catch (_: InvalidProtocolBufferException) {
            throw ParseException()
        }
    }

    override fun setSizeLimit(newLimit: Int) = impl.setSizeLimit(newLimit)
}
