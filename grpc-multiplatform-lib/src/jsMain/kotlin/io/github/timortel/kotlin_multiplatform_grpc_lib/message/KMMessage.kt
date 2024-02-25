package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import io.github.timortel.kotlin_multiplatform_grpc_lib.JSPB
import io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_lib.io.toByteArray

actual interface KMMessage {

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    fun serialize(): ByteArray {
        val stream = JSPB.BinaryWriter()
        serialize(CodedOutputStream(stream))

        return stream.getResultBuffer().toByteArray()
    }

    /**
     * Serializes this message and writes it directly to the [CodedOutputStream].
     */
    fun serialize(stream: CodedOutputStream)
}

val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
