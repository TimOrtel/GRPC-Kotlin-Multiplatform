package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.JSPB
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.util.common
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

val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
