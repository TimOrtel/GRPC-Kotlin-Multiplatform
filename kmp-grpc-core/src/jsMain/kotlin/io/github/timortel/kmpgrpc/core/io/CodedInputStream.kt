package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.JSPB
import io.github.timortel.kmpgrpc.core.message.DataType
import io.github.timortel.kmpgrpc.core.message.KMMessage
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

actual class CodedInputStream(
    private val impl: JSPB.BinaryReader,
    actual var recursionDepth: Int = 0
) {

    private var lastReadTag: Int = 0

    actual val bytesUntilLimit: Int get() = throw NotImplementedError()
    actual val isAtEnd: Boolean
        get() = impl.decoder.atEnd()

    actual fun readTag(): Int {
        if (isAtEnd) {
            lastReadTag = 0
            return 0
        }
        impl.fieldCursor = impl.decoder.getCursor()
        val tag = impl.decoder.readUnsignedVarint32()

        impl.nextField = tag ushr 3
        impl.nextWireType = tag and 0x7

        lastReadTag = tag

        return tag
    }

    actual fun checkLastTagWas(value: Int) {
        if (lastReadTag != value) {
            throw ParseException()
        }
    }

    actual fun getLastTag(): Int = lastReadTag

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

    actual fun readDouble(): Double = impl.decoder.readDouble()

    actual fun readFloat(): Float = impl.decoder.readFloat()

    actual fun readUInt64(): ULong = impl.decoder.readUnsignedVarint64().toLong().toULong()

    actual fun readInt64(): Long = impl.decoder.readSignedVarint64().toLong()

    actual fun readInt32(): Int = impl.decoder.readSignedVarint32()

    actual fun readFixed32(): UInt = impl.decoder.readUint32().toInt().toUInt()

    actual fun readFixed64(): ULong = impl.decoder.readUint64().toLong().toULong()

    actual fun readBool(): Boolean = impl.decoder.readBool()

    actual fun readString(): String = impl.readString()

    /**
     * Read a [KMMessage] from the stream.
     */
    actual fun <M : KMMessage> readKMMessage(messageFactory: (CodedInputStream) -> M): M {
        return io.github.timortel.kmpgrpc.core.io.readKMMessage(this, messageFactory)
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
        val bytes = impl.readBytes() as Uint8Array
        return Int8Array(bytes.buffer, bytes.byteOffset, bytes.length).unsafeCast<ByteArray>()
    }

    actual fun readByteArray(): ByteArray = throw NotImplementedError()

    actual fun readUInt32(): UInt = impl.decoder.readUnsignedVarint32().toUInt()

    actual fun readEnum(): Int = impl.decoder.readSignedVarint64().toInt()

    actual fun readSFixed32(): Int = impl.decoder.readInt32().toInt()

    actual fun readSFixed64(): Long = impl.decoder.readInt64().toLong()

    actual fun readSInt32(): Int = impl.decoder.readZigzagVarint32().toInt()

    actual fun readSInt64(): Long = impl.decoder.readZigzagVarint64().toLong()

    actual fun readRawVarint32(): Int = impl.decoder.readSignedVarint32()

    actual fun readRawVarint64(): Long = impl.decoder.readSignedVarint64().toLong()

    actual fun readRawByte(): Byte = throw NotImplementedError("Not available in js")

    actual fun pushLimit(newLimit: Int): Int {
        val oldLimit = impl.decoder.getEnd()
        impl.decoder.setEnd(impl.decoder.getCursor() + newLimit)
        return oldLimit
    }

    actual fun popLimit(oldLimit: Int) {
        impl.decoder.setEnd(oldLimit)
    }

    actual fun setSizeLimit(newLimit: Int): Int = throw NotImplementedError()
}