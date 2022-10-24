package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import com.google.protobuf.CodedInputStream
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.DataType
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import kotlin.jvm.Throws

actual class CodedInputStream(private val impl: CodedInputStream, actual var recursionDepth: Int = 0) {


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

    actual fun readFixed32(): Int = impl.readFixed32()

    actual fun readBool(): Boolean = impl.readBool()

    actual fun readString(): String = impl.readString()

    actual fun readBytes(): ByteArray = impl.readBytes().toByteArray()

    actual fun readByteArray(): ByteArray = impl.readByteArray()

    actual fun readUInt32(): UInt = impl.readUInt32().toUInt()

    actual fun readEnum(): Int = impl.readEnum()

    actual fun readSFixed32(): Int = impl.readSFixed32()

    actual fun readSFixed64(): Long = impl.readSFixed64()

    actual fun readSInt32(): Int = impl.readSFixed32()

    actual fun readSInt64(): Long = impl.readSInt64()

    actual fun readRawVarint32(): Int = impl.readRawVarint32()

    actual fun readRawVarint64(): Long = impl.readRawVarint64()

    actual fun readRawByte(): Byte = impl.readRawByte()

    actual fun pushLimit(newLimit: Int): Int = impl.pushLimit(newLimit)

    actual fun popLimit(oldLimit: Int) = impl.popLimit(oldLimit)

    actual fun <M : KMMessage> readKMMessage(messageFactory: (io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedInputStream) -> M): M {
        return readKMMessage(this, messageFactory)
    }

    @Throws(ParseException::class)
    actual fun checkLastTagWas(value: Int) = impl.checkLastTagWas(value)

    actual fun setSizeLimit(newLimit: Int) = impl.setSizeLimit(newLimit)

    actual fun <K, V> readMapEntry(
        map: MutableMap<K, V>,
        keyDataType: DataType,
        valueDataType: DataType,
        defaultKey: K?,
        defaultValue: V?,
        readKey: io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedInputStream.() -> K,
        readValue: io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedInputStream.() -> V
    ) {

    }
}