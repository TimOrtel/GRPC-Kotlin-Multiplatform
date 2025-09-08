package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

internal typealias WriteScalar<T> = CodedOutputStream.(fieldNumber: Int, value: T) -> Unit

/**
 * Represents a generic field type for protocol buffer serialization and deserialization.
 */
@InternalKmpGrpcApi
sealed class FieldType<T>(
    internal val writeScalar: WriteScalar<T>,
    internal val read: CodedInputStream.() -> T
) {
    sealed class RepeatableFieldType<T>(
        writeScalar: WriteScalar<T>,
        read: CodedInputStream.() -> T
    ) : FieldType<T>(writeScalar, read)

    @InternalKmpGrpcApi
    sealed class PackableFieldType<T>(
        writeScalar: WriteScalar<T>,
        internal val writeRepeated: CodedOutputStream.(fieldNumber: Int, value: List<T>, tag: UInt) -> Unit,
        read: CodedInputStream.() -> T
    ) : RepeatableFieldType<T>(writeScalar, read)

    @InternalKmpGrpcApi
    sealed class NonPackableFieldType<T>(
        writeScalar: WriteScalar<T>,
        internal val writeRepeated: CodedOutputStream.(fieldNumber: Int, value: List<T>) -> Unit,
        read: CodedInputStream.() -> T
    ) : RepeatableFieldType<T>(writeScalar, read)

    @InternalKmpGrpcApi
    data object Float : PackableFieldType<kotlin.Float>(
        CodedOutputStream::writeFloat,
        CodedOutputStream::writeFloatArray,
        CodedInputStream::readFloat
    )

    @InternalKmpGrpcApi
    data object Double : PackableFieldType<kotlin.Double>(
        CodedOutputStream::writeDouble,
        CodedOutputStream::writeDoubleArray,
        CodedInputStream::readDouble
    )

    @InternalKmpGrpcApi
    data object Int32 : PackableFieldType<Int>(
        CodedOutputStream::writeInt32,
        CodedOutputStream::writeInt32Array,
        CodedInputStream::readInt32
    )

    @InternalKmpGrpcApi
    data object Int64 : PackableFieldType<Long>(
        CodedOutputStream::writeInt64,
        CodedOutputStream::writeInt64Array,
        CodedInputStream::readInt64
    )

    @InternalKmpGrpcApi
    data object UInt32 : PackableFieldType<UInt>(
        CodedOutputStream::writeUInt32,
        CodedOutputStream::writeUInt32Array,
        CodedInputStream::readUInt32
    )

    @InternalKmpGrpcApi
    data object UInt64 : PackableFieldType<ULong>(
        CodedOutputStream::writeUInt64,
        CodedOutputStream::writeUInt64Array,
        CodedInputStream::readUInt64
    )

    @InternalKmpGrpcApi
    data object Fixed32 : PackableFieldType<UInt>(
        CodedOutputStream::writeFixed32,
        CodedOutputStream::writeFixed32Array,
        CodedInputStream::readFixed32
    )

    @InternalKmpGrpcApi
    data object Fixed64 : PackableFieldType<ULong>(
        CodedOutputStream::writeFixed64,
        CodedOutputStream::writeFixed64Array,
        CodedInputStream::readFixed64
    )

    @InternalKmpGrpcApi
    data object SFixed32 : PackableFieldType<Int>(
        CodedOutputStream::writeSFixed32,
        CodedOutputStream::writeSFixed32Array,
        CodedInputStream::readSFixed32
    )

    @InternalKmpGrpcApi
    data object SFixed64 : PackableFieldType<Long>(
        CodedOutputStream::writeSFixed64,
        CodedOutputStream::writeSFixed64Array,
        CodedInputStream::readSFixed64
    )

    @InternalKmpGrpcApi
    data object SInt32 : PackableFieldType<Int>(
        CodedOutputStream::writeSInt32,
        CodedOutputStream::writeSInt32Array,
        CodedInputStream::readSInt32
    )

    @InternalKmpGrpcApi
    data object SInt64 : PackableFieldType<Long>(
        CodedOutputStream::writeSInt64,
        CodedOutputStream::writeSInt64Array,
        CodedInputStream::readSInt64
    )

    @InternalKmpGrpcApi
    data object Bool : PackableFieldType<Boolean>(
        CodedOutputStream::writeBool,
        CodedOutputStream::writeBoolArray,
        CodedInputStream::readBool
    )

    @InternalKmpGrpcApi
    data object String : NonPackableFieldType<kotlin.String>(
        CodedOutputStream::writeString,
        CodedOutputStream::writeStringArray,
        CodedInputStream::readString
    )

    @InternalKmpGrpcApi
    data object Bytes : NonPackableFieldType<ByteArray>(
        CodedOutputStream::writeBytes,
        CodedOutputStream::writeBytesArray,
        CodedInputStream::readBytes
    )

    @InternalKmpGrpcApi
    data class Message<M : io.github.timortel.kmpgrpc.core.message.Message>(internal val deserializer: MessageDeserializer<M>) :
        NonPackableFieldType<io.github.timortel.kmpgrpc.core.message.Message>(
            CodedOutputStream::writeMessage,
            CodedOutputStream::writeMessageArray,
            { readMessage(deserializer) }
        )

    @InternalKmpGrpcApi
    data object Enum : PackableFieldType<Int>(
        CodedOutputStream::writeEnum,
        CodedOutputStream::writeEnumArrayRaw,
        CodedInputStream::readEnum
    )
}
