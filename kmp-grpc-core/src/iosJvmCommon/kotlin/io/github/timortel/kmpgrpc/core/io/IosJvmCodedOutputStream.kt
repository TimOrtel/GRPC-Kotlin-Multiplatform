package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.KMMessage

/**
 * An internal interface that extends [CodedOutputStream] to provide specialized methods for encoding messages in the iOS and JVM platforms.
 * This interface includes shared functionality for dealing with repeated field arrays, map objects, and optimized serialization using specific wire formats.
 */
internal interface IosJvmCodedOutputStream : CodedOutputStream {

    fun writeMessage(fieldNumber: Int, value: KMMessage, requiredSize: UInt) {
        writeUInt32NoTag(wireFormatMakeTag(fieldNumber, WireFormat.LENGTH_DELIMITED).toUInt())
        writeUInt32NoTag(requiredSize)

        value.serialize(this)
    }

    fun writeMessageArray(fieldNumber: Int, values: List<KMMessage>, requiredSize: (KMMessage) -> UInt) {
        values.forEach { writeMessage(fieldNumber, it, requiredSize(it)) }
    }

    fun <T> writeArray(
        fieldNumber: Int,
        values: Collection<T>,
        tag: UInt,
        computeSizeNoTag: (T) -> Int,
        writeNoTag: (T) -> Unit,
        writeTag: (Int, T) -> Unit
    ) {
        if (tag != 0u) {
            if (values.isEmpty()) return

            val dataSize = values.sumOf(computeSizeNoTag)
            writeRawVarint32(tag.toInt())
            writeRawVarint32(dataSize)

            values.forEach(writeNoTag)
        } else {
            values.forEach { writeTag(fieldNumber, it) }
        }
    }

    override fun <K, V> writeMap(
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
            writeInt32NoTag(tag)
            //Write the size of the message
            val msgSize =
                getKeySize(kMapKeyFieldNumber, key) + getValueSize(kMapValueFieldNumber, value)
            writeInt32NoTag(msgSize)
            //Write fields
            writeKey(kMapKeyFieldNumber, key)
            writeValue(kMapValueFieldNumber, value)
        }
    }
}
