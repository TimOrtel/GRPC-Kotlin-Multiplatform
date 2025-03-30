package io.github.timortel.kmpgrpc.core.io


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