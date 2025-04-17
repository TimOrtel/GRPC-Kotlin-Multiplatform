package io.github.timortel.kmpgrpc.core.io

/**
 * Writes the contents of a map to a CodedOutputStream, allowing for custom serialization of keys and values.
 *
 * @param K The type of keys in the map.
 * @param V The type of values in the map.
 * @param stream The output stream to which the map data will be written.
 * @param fieldNumber The field number used to identify the map in serialized data.
 * @param map The map whose contents are to be written to the output stream.
 * @param writeKey A function specifying how to serialize the key of each entry in the map.
 * @param writeValue A function specifying how to serialize the value of each entry in the map.
 */
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

/**
 * Writes a collection of values to a CodedOutputStream.
 *
 * @param stream The CodedOutputStream to write to.
 * @param fieldNumber The field number associated with the data.
 * @param values The collection of values to write to the stream.
 * @param tag The tag used for delimited writing. If set to 0u, values will be written with tags individually.
 * @param writeNoTag A function defining how to write an individual value without a tag.
 * @param writeWithTag A function defining how to write an individual value with a tag.
 */
fun <T> writeArray(
    stream: CodedOutputStream,
    fieldNumber: Int,
    values: Collection<T>,
    tag: UInt,
    writeNoTag: (T) -> Unit,
    writeWithTag: (Int, T) -> Unit
) {
    if (tag != 0u) {
        if (values.isEmpty()) return

        val mark = stream.impl.beginDelimited(fieldNumber)

        values.forEach(writeNoTag)

        stream.impl.endDelimited(mark)
    } else {
        values.forEach { writeWithTag(fieldNumber, it) }
    }
}
