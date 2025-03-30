package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import java.nio.ByteBuffer

/**
 * On the jvm, we wrap [com.google.protobuf.CodedOutputStream] to serialize our messages.
 */
actual interface KMMessage {

    /**
     * The size of this message when serialized in bytes.
     */
    val requiredSize: Int

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    fun serialize(): ByteArray {
        val data = ByteArray(requiredSize)
        val stream = com.google.protobuf.CodedOutputStream.newInstance(ByteBuffer.wrap(data))
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