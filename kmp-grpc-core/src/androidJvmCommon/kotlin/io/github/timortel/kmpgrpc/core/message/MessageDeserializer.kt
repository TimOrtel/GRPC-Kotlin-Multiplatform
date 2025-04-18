package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.JvmCodedInputStream

actual interface MessageDeserializer<T : KMMessage> {
    actual fun deserialize(`data`: ByteArray): T {
        val stream = JvmCodedInputStream(com.google.protobuf.CodedInputStream.newInstance(data))
        return deserialize(stream)
    }

    actual fun deserialize(stream: CodedInputStream): T

    fun deserialize(stream: com.google.protobuf.CodedInputStream): T {
        return deserialize(JvmCodedInputStream(stream))
    }
}
