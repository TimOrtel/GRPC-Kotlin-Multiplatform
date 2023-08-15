package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

fun writeKMMessage(
    stream: CodedOutputStream,
    fieldNumber: Int,
    msg: KMMessage,
    requiredSize: UInt,
    writeMessage: (KMMessage, CodedOutputStream) -> Unit
) {
    stream.writeUInt32NoTag(wireFormatMakeTag(fieldNumber, WireFormat.LENGTH_DELIMITED).toUInt())
    stream.writeUInt32NoTag(requiredSize)

    writeMessage(msg, stream)
}

fun writeMessageList(
    stream: CodedOutputStream,
    fieldNumber: Int,
    values: List<KMMessage>,
    requiredSize: (KMMessage) -> UInt,
    writeMessage: (KMMessage, CodedOutputStream) -> Unit
) {
    values.forEach { writeKMMessage(stream, fieldNumber, it, requiredSize(it), writeMessage) }
}

fun <T> writeArray(
    stream: CodedOutputStream,
    fieldNumber: Int,
    values: Collection<T>,
    tag: UInt,
    computeSizeNoTag: (T) -> ULong,
    writeNoTag: (T) -> Unit,
    writeTag: (Int, T) -> Unit
) {
    if (tag != 0u) {
        if (values.isEmpty()) return

        val dataSize = values.sumOf(computeSizeNoTag)
        stream.writeRawVarint32(tag.toInt())
        stream.writeRawVarint32(dataSize.toInt())

        values.forEach(writeNoTag)
    } else {
        values.forEach { writeTag(fieldNumber, it) }
    }
}