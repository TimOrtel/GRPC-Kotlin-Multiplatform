package io.github.timortel.kotlin_multiplatform_grpc_lib.io

fun <K, V> writeMap(
    stream: CodedOutputStream,
    fieldNumber: Int,
    map: Map<K, V>,
    writeKey: CodedOutputStream.(fieldNumber: Int, K) -> Unit,
    writeValue: CodedOutputStream.(fieldNumber: Int, V) -> Unit
) {
    map.forEach { (key, value) ->
        stream.impl.beginSubMessage(fieldNumber)

        writeKey(stream, kMapKeyFieldNumber, key)
        writeValue(stream, kMapValueFieldNumber, value)

        stream.impl.endSubMessage()
    }
}