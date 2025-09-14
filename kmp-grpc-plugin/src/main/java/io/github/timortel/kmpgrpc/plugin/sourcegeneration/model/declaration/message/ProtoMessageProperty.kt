package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.DeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildPropertyNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile

interface ProtoMessageProperty : ProtoChildProperty {

    /**
     * The message this field applies to.
     * Note: In the case of extensions, the [message] and the [declarationResolver] might differ.
     */
    val message: ProtoMessage

    val file: ProtoFile

    override val resolvingParent: ProtoChildPropertyNameResolver
        get() = message

    val declarationResolver: DeclarationResolver
}
