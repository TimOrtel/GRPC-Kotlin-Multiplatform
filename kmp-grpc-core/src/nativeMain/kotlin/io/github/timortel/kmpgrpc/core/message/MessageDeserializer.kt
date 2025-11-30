package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.internal.CodedInputStreamImpl
import io.github.timortel.kmpgrpc.core.message.extensions.ExtensionRegistry
import kotlinx.io.Buffer

actual interface MessageDeserializer<T : Message> {

    actual fun deserialize(`data`: ByteArray, extensionRegistry: ExtensionRegistry<T>): T {
        val buffer = Buffer()
        buffer.write(data)

        val stream = CodedInputStreamImpl(buffer)
        return deserialize(stream, extensionRegistry)
    }

    actual fun deserialize(stream: CodedInputStream, extensionRegistry: ExtensionRegistry<T>): T
}
