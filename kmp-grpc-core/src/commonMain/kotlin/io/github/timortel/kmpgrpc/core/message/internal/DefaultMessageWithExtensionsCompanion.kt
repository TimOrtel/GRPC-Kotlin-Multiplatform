package io.github.timortel.kmpgrpc.core.message.internal

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.message.MessageWithExtensionsCompanion
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

@InternalKmpGrpcApi
expect interface DefaultMessageWithExtensionsCompanion<T : Message> :
    MessageDeserializer<T>,
    MessageWithExtensionsCompanion<T> {

    open override fun deserialize(data: ByteArray): T

    open override fun deserialize(stream: CodedInputStream): T
}
