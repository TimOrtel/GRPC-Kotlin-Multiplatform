package io.github.timortel.kotlin_multiplatform_grpc_lib.io

actual class CodedInputStream {

    actual var recursionLimit: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var sizeLimit: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    actual val bytesUntilLimit: Int
        get() = TODO("Not yet implemented")
    actual val isAtEnd: Boolean
        get() = TODO("Not yet implemented")

    actual fun readTag(): Int {
        TODO("Not yet implemented")
    }

    @Throws(ParseException::class)
    actual fun checkLastTagWas(value: Int) {
    }

    actual fun getLastTag(): Int {
        TODO("Not yet implemented")
    }

    actual fun skipField(tag: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun skipMessage() {
    }

    actual fun readDouble(): Double {
        TODO("Not yet implemented")
    }

    actual fun readFloat(): Float {
        TODO("Not yet implemented")
    }

    actual fun readUInt64(): ULong {
        TODO("Not yet implemented")
    }

    actual fun readInt64(): Long {
        TODO("Not yet implemented")
    }

    actual fun readInt32(): Int {
        TODO("Not yet implemented")
    }

    actual fun readFixed32(): Int {
        TODO("Not yet implemented")
    }

    actual fun readBool(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun readString(): String {
        TODO("Not yet implemented")
    }

    actual fun readBytes(): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun readByteArray(): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun readUInt32(): UInt {
        TODO("Not yet implemented")
    }

    actual fun readEnum(): Int {
        TODO("Not yet implemented")
    }

    actual fun readSFixed32(): Int {
        TODO("Not yet implemented")
    }

    actual fun readSFixed64(): Long {
        TODO("Not yet implemented")
    }

    actual fun readSInt32(): Int {
        TODO("Not yet implemented")
    }

    actual fun readSInt64(): Long {
        TODO("Not yet implemented")
    }

    actual fun readRawVarint32(): Int {
        TODO("Not yet implemented")
    }

    actual fun readRawVarint64(): Long {
        TODO("Not yet implemented")
    }

    actual fun readRawByte(): Byte {
        TODO("Not yet implemented")
    }

    actual fun pushLimit(newLimit: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun popLimit(oldLimit: Int) {
    }
}