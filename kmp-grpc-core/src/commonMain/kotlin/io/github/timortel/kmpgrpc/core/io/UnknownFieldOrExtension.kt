package io.github.timortel.kmpgrpc.core.io

import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.extensions.Extension

sealed interface UnknownFieldOrExtension {
    data class UnknownField(val field: io.github.timortel.kmpgrpc.core.message.UnknownField) : UnknownFieldOrExtension

    sealed interface Extension<M : Message, T> : UnknownFieldOrExtension

    data class ScalarExtension<M : Message, T>(val extension: Extension.ScalarValueExtension<M, T>, val value: T) : Extension<M, T>
    data class RepeatedExtension<M : Message, T>(val extension: Extension.RepeatedValueExtension<M, T>, val value: List<T>) : Extension<M, T>
}
