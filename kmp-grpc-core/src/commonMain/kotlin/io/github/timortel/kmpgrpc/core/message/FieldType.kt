package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.io.DataSize
import io.github.timortel.kmpgrpc.core.message.extensions.ExtensionRegistry
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

internal typealias WriteScalar<T> = CodedOutputStream.(fieldNumber: Int, value: T) -> Unit

/**
 * Represents a generic field type for protocol buffer serialization and deserialization.
 */
@InternalKmpGrpcApi
sealed class FieldType<T>(
    internal val writeScalar: WriteScalar<T>,
    internal val read: CodedInputStream.() -> T,
    internal val computeSizeNoTag: (T) -> Int
) {
    sealed class RepeatableFieldType<T>(
        writeScalar: WriteScalar<T>,
        read: CodedInputStream.() -> T,
        computeSizeNoTag: (T) -> Int
    ) : FieldType<T>(writeScalar, read, computeSizeNoTag)

    @InternalKmpGrpcApi
    sealed class PackableFieldType<T>(
        writeScalar: WriteScalar<T>,
        internal val writeRepeated: CodedOutputStream.(fieldNumber: Int, value: List<T>, tag: UInt) -> Unit,
        read: CodedInputStream.() -> T,
        computeSizeNoTag: (T) -> Int
    ) : RepeatableFieldType<T>(writeScalar, read, computeSizeNoTag)

    @InternalKmpGrpcApi
    sealed class NonPackableFieldType<T>(
        writeScalar: WriteScalar<T>,
        internal val writeRepeated: CodedOutputStream.(fieldNumber: Int, value: List<T>) -> Unit,
        read: CodedInputStream.() -> T,
        computeSizeNoTag: (T) -> Int
    ) : RepeatableFieldType<T>(writeScalar, read, computeSizeNoTag)

    @InternalKmpGrpcApi
    data object Float : PackableFieldType<kotlin.Float>(
        CodedOutputStream::writeFloat,
        CodedOutputStream::writeFloatArray,
        CodedInputStream::readFloat,
        DataSize::computeFloatSizeNoTag
    )

    @InternalKmpGrpcApi
    data object Double : PackableFieldType<kotlin.Double>(
        CodedOutputStream::writeDouble,
        CodedOutputStream::writeDoubleArray,
        CodedInputStream::readDouble,
        DataSize::computeDoubleSizeNoTag
    )

    @InternalKmpGrpcApi
    data object Int32 : PackableFieldType<Int>(
        CodedOutputStream::writeInt32,
        CodedOutputStream::writeInt32Array,
        CodedInputStream::readInt32,
        DataSize::computeInt32SizeNoTag
    )

    @InternalKmpGrpcApi
    data object Int64 : PackableFieldType<Long>(
        CodedOutputStream::writeInt64,
        CodedOutputStream::writeInt64Array,
        CodedInputStream::readInt64,
        DataSize::computeInt64SizeNoTag
    )

    @InternalKmpGrpcApi
    data object UInt32 : PackableFieldType<UInt>(
        CodedOutputStream::writeUInt32,
        CodedOutputStream::writeUInt32Array,
        CodedInputStream::readUInt32,
        DataSize::computeUInt32SizeNoTag
    )

    @InternalKmpGrpcApi
    data object UInt64 : PackableFieldType<ULong>(
        CodedOutputStream::writeUInt64,
        CodedOutputStream::writeUInt64Array,
        CodedInputStream::readUInt64,
        DataSize::computeUInt64SizeNoTag
    )

    @InternalKmpGrpcApi
    data object Fixed32 : PackableFieldType<UInt>(
        CodedOutputStream::writeFixed32,
        CodedOutputStream::writeFixed32Array,
        CodedInputStream::readFixed32,
        DataSize::computeFixed32SizeNoTag
    )

    @InternalKmpGrpcApi
    data object Fixed64 : PackableFieldType<ULong>(
        CodedOutputStream::writeFixed64,
        CodedOutputStream::writeFixed64Array,
        CodedInputStream::readFixed64,
        DataSize::computeFixed64SizeNoTag
    )

    @InternalKmpGrpcApi
    data object SFixed32 : PackableFieldType<Int>(
        CodedOutputStream::writeSFixed32,
        CodedOutputStream::writeSFixed32Array,
        CodedInputStream::readSFixed32,
        DataSize::computeSFixed32SizeNoTag
    )

    @InternalKmpGrpcApi
    data object SFixed64 : PackableFieldType<Long>(
        CodedOutputStream::writeSFixed64,
        CodedOutputStream::writeSFixed64Array,
        CodedInputStream::readSFixed64,
        DataSize::computeSFixed64SizeNoTag
    )

    @InternalKmpGrpcApi
    data object SInt32 : PackableFieldType<Int>(
        CodedOutputStream::writeSInt32,
        CodedOutputStream::writeSInt32Array,
        CodedInputStream::readSInt32,
        DataSize::computeSInt32SizeNoTag
    )

    @InternalKmpGrpcApi
    data object SInt64 : PackableFieldType<Long>(
        CodedOutputStream::writeSInt64,
        CodedOutputStream::writeSInt64Array,
        CodedInputStream::readSInt64,
        DataSize::computeSInt64SizeNoTag
    )

    @InternalKmpGrpcApi
    data object Bool : PackableFieldType<Boolean>(
        CodedOutputStream::writeBool,
        CodedOutputStream::writeBoolArray,
        CodedInputStream::readBool,
        DataSize::computeBoolSizeNoTag
    )

    @InternalKmpGrpcApi
    data object String : NonPackableFieldType<kotlin.String>(
        CodedOutputStream::writeString,
        CodedOutputStream::writeStringArray,
        CodedInputStream::readString,
        DataSize::computeStringSizeNoTag
    )

    @InternalKmpGrpcApi
    data object Bytes : NonPackableFieldType<ByteArray>(
        CodedOutputStream::writeBytes,
        CodedOutputStream::writeBytesArray,
        CodedInputStream::readBytes,
        DataSize::computeByteArraySizeNoTag
    )

    @InternalKmpGrpcApi
    data class Message<M : io.github.timortel.kmpgrpc.core.message.Message>(
        internal val deserializer: MessageDeserializer<M>,
        internal val extensionRegistry: ExtensionRegistry<M>
    ) :
        NonPackableFieldType<M>(
            CodedOutputStream::writeMessage,
            CodedOutputStream::writeMessageArray,
            { readMessage(deserializer, extensionRegistry) },
            DataSize::computeMessageSizeNoTag
        )

    @InternalKmpGrpcApi
    class Enum<T : io.github.timortel.kmpgrpc.core.message.Enum>(companion: EnumCompanion<T>) : PackableFieldType<T>(
        CodedOutputStream::writeEnum,
        { fieldNumber, values, tag -> writeEnumArrayRaw(fieldNumber, values.map { it.number }, tag) },
        { companion.getEnumForNumber(readEnum()) },
        { DataSize.computeEnumSizeNoTag(it.number) }
    )
}
