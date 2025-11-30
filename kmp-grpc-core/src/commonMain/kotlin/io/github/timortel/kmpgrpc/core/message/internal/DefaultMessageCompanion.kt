package io.github.timortel.kmpgrpc.core.message.internal

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageCompanion
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.message.extensions.ExtensionRegistry
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

@InternalKmpGrpcApi
interface DefaultMessageCompanion<T : Message> : MessageDeserializer<T>, MessageCompanion<T> {
    override fun deserialize(data: ByteArray): T {
        return deserialize(data, ExtensionRegistry.empty())
    }

    override fun deserialize(stream: CodedInputStream): T {
        return deserialize(stream, ExtensionRegistry.empty())
    }
}
