package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.common
import io.github.timortel.kmpgrpc.core.external.JSPB
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.io.JsCodedOutputStream
import org.khronos.webgl.Uint8Array

actual interface Message {

    actual val fullName: String

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    fun serializeNative(): Uint8Array {
        val writer = JSPB.BinaryWriter()
        val stream = JsCodedOutputStream(writer)

        serialize(stream)

        return writer.getResultBuffer()
    }

    actual fun serialize(): ByteArray {
        return serializeNative().common
    }

    actual fun serialize(stream: CodedOutputStream)
}
