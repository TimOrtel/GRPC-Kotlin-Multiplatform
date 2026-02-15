package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.CodeNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty

interface ProtoExtensionField : ProtoMessageProperty {

    val parent: Parent

    override val codeNameResolver: CodeNameResolver
        get() = when (val p = parent) {
            is Parent.ExtensionDefinition -> p.ext
            is Parent.Message -> p.message.fieldNameResolver
        }

    sealed interface Parent {
        data class Message(val message: ProtoMessage) : Parent
        data class ExtensionDefinition(val ext: ProtoExtensionDefinition) : Parent
    }
}
