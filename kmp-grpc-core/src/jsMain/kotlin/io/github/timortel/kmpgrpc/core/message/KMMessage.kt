package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.common
import io.github.timortel.kmpgrpc.core.external.JSPB
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import org.khronos.webgl.Uint8Array

actual interface KMMessage {

    actual val fullName: String

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    fun serializeNative(): Uint8Array {
        val writer = JSPB.BinaryWriter()
        val stream = CodedOutputStream(writer)

        serialize(stream)

        return writer.getResultBuffer()
    }

    actual fun serialize(): ByteArray {
        return serializeNative().common
    }

    /**
     * Serializes this message and writes it directly to the [CodedOutputStream].
     */
    fun serialize(stream: CodedOutputStream)
}
