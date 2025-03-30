package io.github.timortel.kmpgrpc.core.io

import com.google.protobuf.InvalidProtocolBufferException
import io.github.timortel.kmpgrpc.core.message.DataType
import io.github.timortel.kmpgrpc.core.message.KMMessage
import kotlin.jvm.Throws

actual class CodedInputStream(
    private val impl: com.google.protobuf.CodedInputStream,
    actual var recursionDepth: Int = 0
) {

    actual val bytesUntilLimit: Int
        get() = impl.bytesUntilLimit

    actual val isAtEnd: Boolean
        get() = impl.isAtEnd

    actual fun readTag(): Int = impl.readTag()

    actual fun getLastTag(): Int = impl.lastTag

    actual fun skipField(tag: Int): Boolean = impl.skipField(tag)

    actual fun skipMessage() = impl.skipMessage()

    actual fun readDouble(): Double = impl.readDouble()

    actual fun readFloat(): Float = impl.readFloat()

    actual fun readUInt64(): ULong = impl.readUInt64().toULong()

    actual fun readInt64(): Long = impl.readInt64()

    actual fun readInt32(): Int = impl.readInt32()

    actual fun readFixed32(): UInt = impl.readFixed32().toUInt()

    actual fun readFixed64(): ULong = impl.readFixed64().toULong()

    actual fun readBool(): Boolean = impl.readBool()

    actual fun readString(): String = impl.readString()

    actual fun readBytes(): ByteArray = impl.readBytes().toByteArray()

    actual fun readByteArray(): ByteArray = impl.readByteArray()

    actual fun readUInt32(): UInt = impl.readUInt32().toUInt()

    actual fun readEnum(): Int = impl.readEnum()

    actual fun readSFixed32(): Int = impl.readSFixed32()

    actual fun readSFixed64(): Long = impl.readSFixed64()

    actual fun readSInt32(): Int = impl.readSInt32()

    actual fun readSInt64(): Long = impl.readSInt64()

    actual fun readRawVarint32(): Int = impl.readRawVarint32()

    actual fun readRawVarint64(): Long = impl.readRawVarint64()

    actual fun readRawByte(): Byte = impl.readRawByte()

    actual fun pushLimit(newLimit: Int): Int = impl.pushLimit(newLimit)

    actual fun popLimit(oldLimit: Int) = impl.popLimit(oldLimit)

    actual fun <M : KMMessage> readKMMessage(messageFactory: (CodedInputStream) -> M): M {
        return io.github.timortel.kmpgrpc.core.io.readKMMessage(this, messageFactory)
    }

    @Throws(ParseException::class)
    actual fun checkLastTagWas(value: Int) {
        try {
            impl.checkLastTagWas(value)
        } catch (_: InvalidProtocolBufferException) {
            throw ParseException()
        }
    }

    actual fun setSizeLimit(newLimit: Int) = impl.setSizeLimit(newLimit)

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
}