package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import io.github.timortel.kotlin_multiplatform_grpc_lib.JSPB
import io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedOutputStream

actual interface KMMessage {

    /**
     * The size this message takes up in a byte array.
     */
    actual val requiredSize: Int

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    fun serialize(): ByteArray {
        val data = ByteArray(requiredSize)
        val stream = JSPB.BinaryWriter()
        serialize(CodedOutputStream(stream))

        return data
    }

    /**
     * Serializes this message and writes it directly to the [CodedOutputStream].
     */
    fun serialize(stream: CodedOutputStream)
}

val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
val requiredSizeMessage: (KMMessage) -> UInt = { message -> message.requiredSize.toUInt() }