package io.github.timortel.kmpgrpc.core.message

import cocoapods.Protobuf.GPBCodedInputStream
import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.IosCodedInputStream
import io.github.timortel.kmpgrpc.core.native
import platform.Foundation.NSData

actual interface MessageDeserializer<T : KMMessage> {

    actual fun deserialize(`data`: ByteArray): T {
        return deserialize(data.native)
    }

    actual fun deserialize(stream: CodedInputStream): T

    fun deserialize(`data`: NSData): T {
        val stream = IosCodedInputStream(GPBCodedInputStream(data))
        return deserialize(stream)
    }
}
