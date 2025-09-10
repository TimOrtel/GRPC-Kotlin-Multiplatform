package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.UnknownFieldOrExtension
import io.github.timortel.kmpgrpc.core.message.extensions.MessageExtensionsBuilder
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

@InternalKmpGrpcApi
fun <M : Message> mergeUnknownFieldOrExtension(
    fieldOrExtension: UnknownFieldOrExtension<M, Any>?,
    unknownFields: MutableList<UnknownField>,
    extensionBuilder: MessageExtensionsBuilder<M>
) {
    when (fieldOrExtension) {
        is UnknownFieldOrExtension.UnknownField -> unknownFields.add(fieldOrExtension.field)
        is UnknownFieldOrExtension.RepeatedExtension<M, Any> -> {
            extensionBuilder.put(fieldOrExtension.extension, fieldOrExtension.value)
        }
        is UnknownFieldOrExtension.ScalarExtension<M, Any> -> {
            extensionBuilder.put(fieldOrExtension.extension, fieldOrExtension.value)
        }
        null -> {}
    }
}
