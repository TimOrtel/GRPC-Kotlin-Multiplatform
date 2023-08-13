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

fun <K, V> writeMap(
    stream: CodedOutputStream,
    fieldNumber: Int,
    map: Map<K, V>,
    getKeySize: (fieldNumber: Int, key: K) -> Int,
    getValueSize: (fieldNumber: Int, value: V) -> Int,
    writeKey: CodedOutputStream.(fieldNumber: Int, K) -> Unit,
    writeValue: CodedOutputStream.(fieldNumber: Int, V) -> Unit
) {
    val tag = wireFormatMakeTag(fieldNumber, WireFormat.LENGTH_DELIMITED)
    map.forEach { (key, value) ->
        //Write tag
        stream.writeInt32NoTag(tag)
        //Write the size of the message
        val msgSize =
            getKeySize(kMapKeyFieldNumber, key) + getValueSize(kMapValueFieldNumber, value)
        stream.writeInt32NoTag(msgSize)
        //Write fields
        writeKey(stream, kMapKeyFieldNumber, key)
        writeValue(stream, kMapValueFieldNumber, value)
    }
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

fun writeArray(
    tag: UInt,
    writeRepeated: () -> Unit,
    writePacked: () -> Unit
) {
    if (tag != 0u) {
        writePacked()
    } else {
        writeRepeated()
    }
}