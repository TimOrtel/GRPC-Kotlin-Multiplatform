package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedOutputStream
import java.nio.ByteBuffer

actual interface KMMessage {

    val requiredSize: Int

    fun serialize(): ByteArray {
        val data = ByteArray(requiredSize)
        val stream = com.google.protobuf.CodedOutputStream.newInstance(ByteBuffer.wrap(data))
        serialize(CodedOutputStream(stream))

        return data
    }

    fun serialize(stream: CodedOutputStream)
}


val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
val requiredSizeMessage: (KMMessage) -> UInt = { message -> message.requiredSize.toUInt() }