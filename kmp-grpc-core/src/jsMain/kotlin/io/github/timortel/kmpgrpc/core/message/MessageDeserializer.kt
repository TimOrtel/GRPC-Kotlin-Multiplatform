package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.util.native
import org.khronos.webgl.Uint8Array

actual interface MessageDeserializer<T : KMMessage> {

    actual fun deserialize(`data`: ByteArray): T {
        return deserialize(data.native)
    }

    fun deserialize(`data`: Uint8Array): T
}
