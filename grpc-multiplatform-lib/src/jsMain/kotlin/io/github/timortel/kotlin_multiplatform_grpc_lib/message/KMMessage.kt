package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedOutputStream
import org.khronos.webgl.Uint8Array

actual interface KMMessage {

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    fun serialize(): Uint8Array

    /**
     * Serializes this message and writes it directly to the [CodedOutputStream].
     */
    fun serialize(stream: CodedOutputStream)
}

val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
