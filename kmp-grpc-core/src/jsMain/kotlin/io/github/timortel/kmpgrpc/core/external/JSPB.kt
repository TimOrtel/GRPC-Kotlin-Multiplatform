package io.github.timortel.kmpgrpc.core.external

import org.khronos.webgl.Uint8Array

@JsModule("google-protobuf")
@JsNonModule
external object JSPB {

    class BinaryWriter {

        @JsName("encoder_")
        val encoder: BinaryEncoder

        fun getResultBuffer(): Uint8Array

        fun writeDouble(field: Int, value: Double)
        fun writeFloat(field: Int, value: Float)
        fun writeInt64(field: Int, value: Long)
        fun writeSint64(field: Int, value: Long)
        fun writeUint64(field: Int, value: dynamic)
        fun writeInt32(field: Int, value: Int)
        fun writeSint32(field: Int, value: Int)
        fun writeUint32(field: Int, value: dynamic)
        fun writeFixed64(field: Int, value: dynamic)
        fun writeFixed32(field: Int, value: dynamic)
        fun writeSfixed64(field: Int, value: dynamic)
        fun writeSfixed32(field: Int, value: dynamic)
        fun writeBool(field: Int, value: Boolean)
        fun writeGroup(field: Int, value: dynamic)
        fun writeBytes(field: Int, value: dynamic)
        fun writeString(field: Int, value: String)
        fun writeMessage(field: Int, value: dynamic, writerCallback: dynamic)
        fun writeEnum(field: Int, value: Int)

        fun writeRepeatedBytes(field: Int, value: dynamic)

        @JsName("beginDelimited_")
        fun beginDelimited(field: Int): dynamic

        @JsName("endDelimited_")
        fun endDelimited(bookmark: dynamic)

        fun beginSubMessage(fieldNumber: Int)

        fun endSubMessage()
    }

    class BinaryReader(bytes: dynamic) {

        @JsName("nextField_")
        var nextField: Int

        @JsName("nextWireType_")
        var nextWireType: Int

        @JsName("decoder_")
        val decoder: BinaryDecoder

        @JsName("fieldCursor_")
        var fieldCursor: dynamic

        fun readDouble(): Double
        fun readInt64(): dynamic
        fun readInt32(): Int

        fun readBool(): Boolean
        fun readBytes(): dynamic
        fun readString(): String
        fun readEnum(): Int

        fun skipField()
    }

    class BinaryDecoder {
        fun setEnd(limit: Int)

        fun getEnd(): Int

        fun atEnd(): Boolean

        fun readInt32(): Number
        fun readInt64(): Number

        fun readZigzagVarint32(): Number
        fun readZigzagVarint64(): Number

        fun readUint32(): Number
        fun readUint64(): Number

        fun readUnsignedVarint32(): Int
        fun readUnsignedVarint64(): Number

        fun readSignedVarint32(): Int
        fun readSignedVarint64(): Number

        fun readFloat(): Float
        fun readDouble(): Double

        fun readBool(): Boolean

        fun getCursor(): Int
    }

    class BinaryEncoder {
        fun writeBool(value: dynamic)
        fun writeFloat(value: dynamic)
        fun writeDouble(value: dynamic)
        fun writeUint32(value: dynamic)
        fun writeUint64(value: dynamic)
        fun writeInt8(value: dynamic)
        fun writeInt32(value: dynamic)
        fun writeInt64(value: dynamic)

        fun writeSignedVarint32(value: dynamic)
        fun writeSignedVarint64(value: dynamic)

        fun writeUnsignedVarint32(value: dynamic)
        fun writeUnsignedVarint64(value: dynamic)

        fun writeZigzagVarint32(value: dynamic)
        fun writeZigzagVarint64(value: dynamic)

        fun writeEnum(value: Int)
        fun writeString(value: String)
    }
}
