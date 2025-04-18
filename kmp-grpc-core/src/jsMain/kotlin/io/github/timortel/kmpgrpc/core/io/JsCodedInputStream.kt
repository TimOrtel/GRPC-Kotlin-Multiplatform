package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.common
import io.github.timortel.kmpgrpc.core.external.JSPB
import org.khronos.webgl.Uint8Array

/**
 * A JavaScript implementation of the `CodedInputStream` abstraction. This class provides methods
 * to read and decode serialized protocol buffer data using a `BinaryReader` implementation.
 *
 * The `JsCodedInputStream` is designed to tightly integrate with JavaScript Protocol Buffers
 * (jspb) binary readers.
 *
 * @constructor
 * Initializes an instance with the provided `BinaryReader`.
 * @param impl The `BinaryReader` used for decoding serialized data.
 */
internal class JsCodedInputStream(
    private val impl: JSPB.BinaryReader
) : CodedInputStream() {

    private var lastReadTag: Int = 0

    override val isAtEnd: Boolean
        get() = impl.decoder.atEnd()

    override fun readTag(): Int {
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

    override fun checkLastTagWas(value: Int) {
        if (lastReadTag != value) {
            throw ParseException()
        }
    }

    override fun skipField(tag: Int): Boolean {
        // On an error skipField throws
        return try {
            impl.skipField()
            true
        } catch (_: Throwable) {
            false
        }
    }

    override fun readDouble(): Double = impl.decoder.readDouble()

    override fun readFloat(): Float = impl.decoder.readFloat()

    override fun readUInt64(): ULong = impl.decoder.readUnsignedVarint64().toLong().toULong()

    override fun readInt64(): Long = impl.decoder.readSignedVarint64().toLong()

    override fun readInt32(): Int = impl.decoder.readSignedVarint32()

    override fun readFixed32(): UInt = impl.decoder.readUint32().toLong().toUInt()

    override fun readFixed64(): ULong = impl.decoder.readUint64().toLong().toULong()

    override fun readBool(): Boolean = impl.decoder.readBool()

    override fun readString(): String = impl.readString()

    override fun readBytes(): ByteArray {
        val bytes = impl.readBytes() as Uint8Array
        return bytes.common
    }

    override fun readByteArray(): ByteArray = throw NotImplementedError()

    override fun readUInt32(): UInt = impl.decoder.readUnsignedVarint32().toUInt()

    override fun readEnum(): Int = impl.decoder.readSignedVarint64().toInt()

    override fun readSFixed32(): Int = impl.decoder.readInt32().toInt()

    override fun readSFixed64(): Long = impl.decoder.readInt64().toLong()

    override fun readSInt32(): Int = impl.decoder.readZigzagVarint32().toInt()

    override fun readSInt64(): Long = impl.decoder.readZigzagVarint64().toLong()

    override fun readRawVarint32(): Int = impl.decoder.readSignedVarint32()

    override fun readRawVarint64(): Long = impl.decoder.readSignedVarint64().toLong()

    override fun readRawByte(): Byte = throw NotImplementedError("Not available in js")

    override fun pushLimit(newLimit: Int): Int {
        val oldLimit = impl.decoder.getEnd()
        impl.decoder.setEnd(impl.decoder.getCursor() + newLimit)
        return oldLimit
    }

    override fun popLimit(oldLimit: Int) {
        impl.decoder.setEnd(oldLimit)
    }

    override fun setSizeLimit(newLimit: Int): Int = throw NotImplementedError()
}
