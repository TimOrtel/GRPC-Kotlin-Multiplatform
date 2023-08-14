package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import io.github.timortel.kotlin_multiplatform_grpc_lib.JSPB
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.DataType
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

actual class CodedInputStream(
    private val impl: JSPB.BinaryReader,
    actual var recursionDepth: Int = 0
) {

    actual val bytesUntilLimit: Int get() = throw NotImplementedError()
    actual val isAtEnd: Boolean
        get() = impl.decoder.atEnd()

    actual fun readTag(): Int {
        if (isAtEnd) return 0
        impl.fieldCursor = impl.decoder.getCursor()
        val tag = impl.decoder.readUnsignedVarint32()

        impl.nextField = tag ushr 3
        impl.nextWireType = tag and 0x7
        return tag
    }

    actual fun checkLastTagWas(value: Int) {
        if (impl.nextField != value) {
            throw ParseException()
        }
    }

    actual fun getLastTag(): Int = impl.nextField

    actual fun skipField(tag: Int): Boolean {
        // On an error skipField throws
        return try {
            impl.skipField()
            true
        } catch (_: Throwable) {
            false
        }
    }

    actual fun skipMessage() {
        while (true) {
            val tag = readTag()
            if (tag == 0 || !skipField(tag)) return
        }
    }

    actual fun readDouble(): Double = impl.readDouble()

    actual fun readFloat(): Float = impl.readFloat()

    actual fun readUInt64(): ULong = impl.readUInt64() as ULong

    actual fun readInt64(): Long = (impl.readInt64() as Number).toLong()

    actual fun readInt32(): Int = impl.readInt32()

    actual fun readFixed32(): Int = impl.readFixed32() as Int

    actual fun readBool(): Boolean = impl.readBool()

    actual fun readString(): String = impl.readString()

    /**
     * Read a [KMMessage] from the stream.
     */
    actual fun <M : KMMessage> readKMMessage(messageFactory: (CodedInputStream) -> M): M {
        return readKMMessage(this, messageFactory)
    }

    /**
     * Read a map entry from the stream following the grpc specification.
     */
    actual fun <K, V> readMapEntry(
        map: MutableMap<K, V>,
        keyDataType: DataType,
        valueDataType: DataType,
        defaultKey: K?,
        defaultValue: V?,
        readKey: CodedInputStream.() -> K,
        readValue: CodedInputStream.() -> V
    ) = readMapEntry(
        this,
        map,
        keyDataType,
        valueDataType,
        defaultKey,
        defaultValue,
        readKey,
        readValue
    )

    actual fun readBytes(): ByteArray =
        Int8Array((impl.readBytes() as Uint8Array).buffer).unsafeCast<ByteArray>()

    actual fun readByteArray(): ByteArray = throw NotImplementedError()

    actual fun readUInt32(): UInt = impl.readUInt32() as UInt

    actual fun readEnum(): Int = impl.readEnum()

    actual fun readSFixed32(): Int = impl.readSfixed32() as Int

    actual fun readSFixed64(): Long = impl.readSfixed64() as Long

    actual fun readSInt32(): Int = impl.readSInt32() as Int

    actual fun readSInt64(): Long = impl.readSInt64() as Long

    actual fun readRawVarint32(): Int = impl.readInt32()

    actual fun readRawVarint64(): Long = impl.readInt64() as Long

    actual fun readRawByte(): Byte = throw NotImplementedError("Not available in js")

    actual fun pushLimit(newLimit: Int): Int {
        val oldLimit = impl.decoder.getEnd()
        impl.decoder.setEnd(newLimit)
        return oldLimit
    }

    actual fun popLimit(oldLimit: Int) {
        impl.decoder.setEnd(oldLimit)
    }

    actual fun setSizeLimit(newLimit: Int): Int = throw NotImplementedError()
}