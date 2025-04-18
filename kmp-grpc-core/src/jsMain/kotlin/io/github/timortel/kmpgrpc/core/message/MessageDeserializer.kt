package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.external.JSPB
import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.JsCodedInputStream
import io.github.timortel.kmpgrpc.core.native
import org.khronos.webgl.Uint8Array

actual interface MessageDeserializer<T : KMMessage> {

    actual fun deserialize(`data`: ByteArray): T {
        return deserialize(data.native)
    }

    fun deserialize(`data`: Uint8Array): T {
        val stream = JsCodedInputStream(JSPB.BinaryReader(data))
        return deserialize(stream)
    }

    actual fun deserialize(stream: CodedInputStream): T
}
