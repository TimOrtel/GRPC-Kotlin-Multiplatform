package io.github.timortel.kmpgrpc.core

import org.khronos.webgl.Uint8Array

@JsModule("google-protobuf")
@JsNonModule
external object JSPB {

    object Message {
        fun initialize(p1: dynamic, optData: dynamic, p3: dynamic, p4: dynamic, p5: dynamic, p6: dynamic)

        fun setProto3StringField(msg: dynamic, fieldNumber: Int, value: String)
        fun setProto3IntField(msg: dynamic, fieldNumber: Int, value: dynamic)
        fun setProto3FloatField(msg: dynamic, fieldNumber: Int, value: dynamic)
        fun setProto3BooleanField(msg: dynamic, fieldNumber: Int, value: Boolean)
        fun setProto3BytesField(msg: dynamic, fieldNumber: Int, value: dynamic)
        fun setProto3EnumField(msg: dynamic, fieldNumber: Int, value: dynamic)

        fun setField(msg: dynamic, fieldNumber: Int, value: dynamic)
        fun setWrapperField(msg: dynamic, fieldNumber: Int, value: dynamic)
        fun setRepeatedWrapperField(msg: dynamic, fieldNumber: dynamic, value: dynamic)

        fun getFieldWithDefault(msg: dynamic, fieldNumber: Int, default: dynamic): dynamic
        fun getBooleanFieldWithDefault(msg: dynamic, fieldNumber: Int, default: Boolean): Boolean
        fun getFloatingPointFieldWithDefault(msg: dynamic, fieldNumber: Int, default: dynamic): dynamic

        fun getRepeatedField(msg: dynamic, fieldNumber: Int): dynamic
        fun getRepeatedBooleanField(msg: dynamic, fieldNumber: Int): dynamic
        fun getRepeatedFloatingPointField(msg: dynamic, fieldNumber: Int): dynamic

        fun computeOneofCase(msg: dynamic, group: dynamic): Int

        fun getWrapperField(msg: dynamic, type: dynamic, fieldNumber: Int): dynamic
        fun getRepeatedWrapperField(msg: dynamic, type: dynamic, fieldNumber: Int): Array<dynamic>

        fun getMapField(msg: dynamic, fieldNumber: Int, noLazyCreate: Boolean, type: dynamic): dynamic

        fun getField(msg: dynamic, fieldNumber: Int): dynamic
    }

    fun inherits(clazz: dynamic, superClazz: dynamic)

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

        fun writeRepeatedString(field: Int, value: dynamic)
        fun writePackedBool(field: Int, value: dynamic)
        fun writeRepeatedBool(field: Int, value: dynamic)
        fun writePackedFloat(field: Int, value: dynamic)
        fun writeRepeatedFloat(field: Int, value: dynamic)
        fun writePackedDouble(field: Int, value: dynamic)
        fun writeRepeatedDouble(field: Int, value: dynamic)
        fun writePackedInt32(field: Int, value: dynamic)
        fun writeRepeatedInt32(field: Int, value: dynamic)
        fun writePackedInt64(field: Int, value: dynamic)
        fun writeRepeatedInt64(field: Int, value: dynamic)
        fun writeRepeatedMessage(field: Int, value: dynamic, writerCallback: dynamic)
        fun writePackedEnum(field: Int, value: dynamic)
        fun writeRepeatedEnum(field: Int, value: dynamic)
        fun writePackedFixed32(field: Int, value: dynamic)
        fun writeRepeatedFixed32(field: Int, value: dynamic)
        fun writePackedFixed64(field: Int, value: dynamic)
        fun writeRepeatedFixed64(field: Int, value: dynamic)

        fun writePackedSfixed32(field: Int, value: dynamic)
        fun writeRepeatedSfixed32(field: Int, value: dynamic)
        fun writePackedSfixed64(field: Int, value: dynamic)
        fun writeRepeatedSfixed64(field: Int, value: dynamic)

        fun writePackedSint32(field: Int, value: dynamic)
        fun writeRepeatedSint32(field: Int, value: dynamic)
        fun writePackedSint64(field: Int, value: dynamic)
        fun writeRepeatedSint64(field: Int, value: dynamic)

        fun writePackedUint32(field: Int, value: dynamic)
        fun writeRepeatedUint32(field: Int, value: dynamic)
        fun writePackedUint64(field: Int, value: dynamic)
        fun writeRepeatedUint64(field: Int, value: dynamic)

        @JsName("appendUint8Array_")
        fun appendUint8Array(value: dynamic)

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

        fun nextField(): Boolean

        fun isEndGroup(): Boolean

        fun getFieldNumber(): Int

        fun readDouble(): Double
        fun readFloat(): Float
        fun readInt64(): dynamic
        fun readSint32(): dynamic
        fun readSint64(): dynamic
        fun readUint64(): dynamic
        fun readInt32(): Int
        fun readUint32(): dynamic
        fun readFixed64(): dynamic
        fun readFixed32(): dynamic
        fun readSfixed32(): dynamic
        fun readSfixed64(): dynamic

        fun readBool(): Boolean
        fun readBytes(): dynamic
        fun readString(): String
        fun readEnum(): Int

        fun readMessage(value: dynamic, readerCallback: dynamic)

        fun readPackedDouble(): Array<Double>
        fun readPackedFloat(): Array<Float>
        fun readPackedInt32(): Array<Int>
        fun readPackedInt64(): Array<Long>
        fun readPackedBool(): Array<Boolean>
        fun readPackedEnum(): Array<Int>

        fun readUnsignedVarint32(): dynamic

        fun skipField()
        fun isDelimited(): Boolean
    }

    object Map {
        fun deserializeBinary(message: dynamic, reader: dynamic, keyReader: dynamic, valueReader: dynamic, valueReaderCallback: dynamic, defaultKey: dynamic, defaultValue: dynamic)
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