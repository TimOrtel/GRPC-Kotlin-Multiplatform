package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.UnknownField

fun readUnknownField(stream: CodedInputStream, tag: Int): UnknownField? {
    val number = wireFormatGetTagFieldNumber(tag)
    val wireTypeNumber = wireFormatGetTagWireType(tag)

    return when (WireFormat.entries.firstOrNull { it.value == wireTypeNumber }) {
        WireFormat.VARINT -> UnknownField.Varint(number, stream.readRawVarint64())
        WireFormat.FIXED32 -> UnknownField.Fixed32(number, stream.readFixed32())
        WireFormat.FIXED64 -> UnknownField.Fixed64(number, stream.readFixed64())
        WireFormat.LENGTH_DELIMITED -> UnknownField.LengthDelimited(number, stream.readBytes())
        WireFormat.START_GROUP -> readGroup(stream, number)
        WireFormat.END_GROUP -> null
        null -> throw ParseException("Could not decipher wire format type")
    }
}

private fun readGroup(stream: CodedInputStream, number: Int): UnknownField.Group {
    checkRecursionLimit(stream)
    stream.recursionDepth++

    val fields = buildList {
        while (true) {
            val tag = stream.readTag()
            if (wireFormatGetTagWireType(tag) == WireFormat.END_GROUP.value || tag == 0) {
                break
            }

            val unknownField = readUnknownField(stream, tag)
            if (unknownField != null) {
                add(unknownField)
            }
        }
    }

    stream.recursionDepth--
    stream.checkLastTagWas(wireFormatMakeTag(number, WireFormat.END_GROUP))

    return UnknownField.Group(number, fields)
}

fun writeUnknownFields(stream: CodedOutputStream, fields: List<UnknownField>) {
    fields.forEach { unknownField ->
        when (unknownField) {
            is UnknownField.Varint -> stream.writeInt64(unknownField.number, unknownField.value)
            is UnknownField.Fixed32 -> stream.writeFixed32(unknownField.number, unknownField.value)
            is UnknownField.Fixed64 -> stream.writeFixed64(unknownField.number, unknownField.value)
            is UnknownField.LengthDelimited -> stream.writeBytes(unknownField.number, unknownField.value)
            is UnknownField.Group -> {
                stream.writeTag(unknownField.number, WireFormat.START_GROUP)
                writeUnknownFields(stream, unknownField.values)
                stream.writeTag(unknownField.number, WireFormat.END_GROUP)
            }
        }
    }
}
