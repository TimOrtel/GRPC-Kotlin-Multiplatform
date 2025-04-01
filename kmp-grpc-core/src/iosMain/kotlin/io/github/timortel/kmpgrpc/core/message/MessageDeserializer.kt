package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.util.native
import platform.Foundation.NSData

actual interface MessageDeserializer<T : KMMessage> {

    actual fun deserialize(`data`: ByteArray): T {
        return deserialize(data.native)
    }

    fun deserialize(`data`: NSData): T
}
