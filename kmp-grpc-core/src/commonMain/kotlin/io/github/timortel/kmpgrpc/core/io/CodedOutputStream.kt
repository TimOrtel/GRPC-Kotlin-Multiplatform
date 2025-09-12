package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.Enum
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.core.message.extensions.Extension
import io.github.timortel.kmpgrpc.core.message.extensions.MessageExtensions
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi
import io.github.timortel.kmpgrpc.shared.internal.io.WireFormat
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Interface that encodes messages to send them over the network connection. Counterpart to [CodedInputStream].
 * See [the java CodedOutputStream implementation](https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java) for further details.
 *
 * This class is not stable for inheritance.
 */
@InternalKmpGrpcApi
interface CodedOutputStream {

    fun writeBool(fieldNumber: Int, value: Boolean)

    fun writeBoolArray(fieldNumber: Int, values: List<Boolean>, tag: UInt)

    fun writeBoolNoTag(value: Boolean)

    fun writeBytes(fieldNumber: Int, value: ByteArray)

    fun writeBytesArray(fieldNumber: Int, values: List<ByteArray>)

    fun writeDouble(fieldNumber: Int, value: Double)

    fun writeDoubleArray(fieldNumber: Int, values: List<Double>, tag: UInt)

    fun writeDoubleNoTag(value: Double)

    fun writeEnum(fieldNumber: Int, value: Int)

    fun writeEnum(fieldNumber: Int, value: Enum)

    fun writeEnumArrayRaw(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeEnumArray(fieldNumber: Int, values: List<Enum>, tag: UInt)

    fun writeEnumNoTag(value: Int)

    fun writeFixed32(fieldNumber: Int, value: UInt)

    fun writeFixed32Array(fieldNumber: Int, values: List<UInt>, tag: UInt)

    fun writeFixed32NoTag(value: UInt)

    fun writeFixed64(fieldNumber: Int, value: ULong)

    fun writeFixed64Array(fieldNumber: Int, values: List<ULong>, tag: UInt)

    fun writeFixed64NoTag(value: ULong)

    fun writeFloat(fieldNumber: Int, value: Float)

    fun writeFloatArray(fieldNumber: Int, values: List<Float>, tag: UInt)

    fun writeFloatNoTag(value: Float)

    fun writeInt32(fieldNumber: Int, value: Int)

    fun writeInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeInt32NoTag(value: Int)

    fun writeInt64(fieldNumber: Int, value: Long)

    fun writeInt64Array(fieldNumber: Int, values: List<Long>, tag: UInt)

    fun writeInt64NoTag(value: Long)

    fun writeMessage(fieldNumber: Int, value: Message)

    fun writeMessageArray(fieldNumber: Int, values: List<Message>)

    fun writeSFixed32(fieldNumber: Int, value: Int)

    fun writeSFixed32Array(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeSFixed32NoTag(value: Int)

    fun writeSFixed64(fieldNumber: Int, value: Long)

    fun writeSFixed64Array(fieldNumber: Int, values: List<Long>, tag: UInt)

    fun writeSFixed64NoTag(value: Long)

    fun writeSInt32(fieldNumber: Int, value: Int)

    fun writeSInt32Array(fieldNumber: Int, values: List<Int>, tag: UInt)

    fun writeSInt32NoTag(value: Int)

    fun writeSInt64(fieldNumber: Int, value: Long)

    fun writeSInt64Array(fieldNumber: Int, values: List<Long>, tag: UInt)

    fun writeSInt64NoTag(value: Long)

    fun writeString(fieldNumber: Int, value: String)

    fun writeStringArray(fieldNumber: Int, values: List<String>)

    fun writeStringNoTag(value: String)

    fun writeTag(fieldNumber: Int, format: WireFormat)

    fun writeUInt32(fieldNumber: Int, value: UInt)

    fun writeUInt32Array(fieldNumber: Int, values: List<UInt>, tag: UInt)

    fun writeUInt32NoTag(value: UInt)

    fun writeUInt64(fieldNumber: Int, value: ULong)

    fun writeUInt64Array(fieldNumber: Int, values: List<ULong>, tag: UInt)

    fun writeUInt64NoTag(value: ULong)

    /**
     * Writes a list of unknown fields to the output stream. Each field in the list is processed
     * based on its concrete type and serialized accordingly.
     *
     * @param fields The list of unknown fields to be written.
     */
    fun writeUnknownFields(fields: List<UnknownField>) {
        fields.forEach { unknownField ->
            when (unknownField) {
                is UnknownField.Varint -> writeInt64(unknownField.number, unknownField.value)
                is UnknownField.Fixed32 -> writeFixed32(unknownField.number, unknownField.value)
                is UnknownField.Fixed64 -> writeFixed64(unknownField.number, unknownField.value)
                is UnknownField.LengthDelimited -> writeBytes(unknownField.number, unknownField.value)
                is UnknownField.Group -> {
                    writeTag(unknownField.number, WireFormat.START_GROUP)
                    writeUnknownFields(unknownField.values)
                    writeTag(unknownField.number, WireFormat.END_GROUP)
                }
            }
        }
    }

    /**
     * Writes a map of key-value pairs to the output stream.
     *
     * @param fieldNumber The field number corresponding to the map field.
     * @param map The map containing the key-value pairs to be written.
     * @param getKeySize A function to calculate the serialized size of the key. Takes the field number and key as parameters.
     * @param getValueSize A function to calculate the serialized size of the value. Takes the field number and value as parameters.
     * @param writeKey A function to write the key to the output stream. Takes the field number and key as parameters.
     * @param writeValue A function to write the value to the output stream. Takes the field number and value as parameters.
     */
    fun <K, V> writeMap(
        fieldNumber: Int,
        map: Map<K, V>,
        getKeySize: (fieldNumber: Int, key: K) -> Int,
        getValueSize: (fieldNumber: Int, value: V) -> Int,
        writeKey: CodedOutputStream.(fieldNumber: Int, K) -> Unit,
        writeValue: CodedOutputStream.(fieldNumber: Int, V) -> Unit
    )

    fun <M : Message> writeMessageExtensions(messageExtensions: MessageExtensions<M>) {
        messageExtensions.scalarMap.forEach { (key, value) ->
            key.fieldType.writeScalar(this, key.fieldNumber, value)
        }

        messageExtensions.repeatedMap.forEach { (extension, value) ->
            when (extension) {
                is Extension.PackableRepeatedValueExtension -> {
                    extension.fieldType.writeRepeated(this, extension.fieldNumber, value, extension.tag)
                }

                is Extension.NonPackableRepeatedValueExtension -> {
                    extension.fieldType.writeRepeated(this, extension.fieldNumber, value)
                }
            }
        }
    }
}
