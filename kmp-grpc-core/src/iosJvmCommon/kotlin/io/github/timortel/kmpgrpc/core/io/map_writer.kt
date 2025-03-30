package io.github.timortel.kmpgrpc.core.io

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