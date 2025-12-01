package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.extensions.Extension
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

@InternalKmpGrpcApi
sealed interface UnknownFieldOrExtension<M : Message, T : Any> {
    data class UnknownField(val field: io.github.timortel.kmpgrpc.core.message.UnknownField) : UnknownFieldOrExtension<Message, Unit>

    sealed interface Extension<M : Message, T : Any> : UnknownFieldOrExtension<M, T>

    data class ScalarExtension<M : Message, T : Any>(val extension: Extension.ScalarValueExtension<M, T>, val value: T) : Extension<M, T>
    data class RepeatedExtension<M : Message, T : Any>(val extension: Extension.RepeatedValueExtension<M, T>, val value: List<T>) : Extension<M, T>
}
