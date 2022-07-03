package io.github.timortel.kotlin_multiplatform_grpc_lib

@JsModule("google-protobuf")
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
        fun getResultBuffer(): dynamic

        fun writeDouble(field: Int, value: Double)
        fun writeFloat(field: Int, value: Float)
        fun writeInt64(field: Int, value: Long)
        fun writeUInt64(field: Int, value: dynamic)
        fun writeInt32(field: Int, value: Int)
        fun writeUInt32(field: Int, value: dynamic)
        fun writeFixed64(field: Int, value: dynamic)
        fun writeFixed32(field: Int, value: dynamic)
        fun writeBool(field: Int, value: Boolean)
        fun writeGroup(field: Int, value: dynamic)
        fun writeBytes(field: Int, value: dynamic)
        fun writeString(field: Int, value: String)
        fun writeMessage(field: Int, value: dynamic, writerCallback: dynamic)
        fun writeEnum(field: Int, value: Int)

        fun writeRepeatedString(field: Int, value: dynamic)
        fun writePackedBool(field: Int, value: dynamic)
        fun writePackedFloat(field: Int, value: dynamic)
        fun writePackedDouble(field: Int, value: dynamic)
        fun writePackedInt32(field: Int, value: dynamic)
        fun writePackedInt64(field: Int, value: dynamic)
        fun writeRepeatedMessage(field: Int, value: dynamic, writerCallback: dynamic)
        fun writePackedEnum(field: Int, value: dynamic)
    }

    class BinaryReader(bytes: dynamic) {
        fun nextField(): Boolean

        fun isEndGroup(): Boolean

        fun getFieldNumber(): Int

        fun readDouble(): Double
        fun readFloat(): Float
        fun readInt64(): dynamic
        fun readUInt64(): dynamic
        fun readInt32(): Int
        fun readUInt32(): dynamic
        fun readFixed64(): dynamic
        fun readFixed32(): dynamic
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

        fun skipField()
        fun isDelimited(): Boolean
    }

    object Map {
        fun deserializeBinary(message: dynamic, reader: dynamic, keyReader: dynamic, valueReader: dynamic, valueReaderCallback: dynamic, defaultKey: dynamic, defaultValue: dynamic)
    }
}