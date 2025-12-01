package io.github.timortel.kmpgrpc.core.message.internal

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.message.MessageWithExtensionsCompanion
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

@InternalKmpGrpcApi
actual interface DefaultMessageWithExtensionsCompanion<T : Message> :
    MessageDeserializer<T>,
    MessageWithExtensionsCompanion<T> {

    actual override fun deserialize(data: ByteArray): T {
        return deserialize(data, defaultExtensionRegistry)
    }

    actual override fun deserialize(stream: CodedInputStream): T {
        return deserialize(stream, defaultExtensionRegistry)
    }
}
