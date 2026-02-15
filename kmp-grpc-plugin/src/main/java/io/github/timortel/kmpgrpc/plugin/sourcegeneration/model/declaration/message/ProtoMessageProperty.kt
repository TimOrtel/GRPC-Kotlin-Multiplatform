package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.DeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.CodeNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile

interface ProtoMessageProperty : ProtoChildProperty {

    /**
     * The message this field applies to.
     * Note: In the case of extensions, the [message] and the [declarationResolver] might differ.
     */
    val message: ProtoMessage

    val file: ProtoFile

    override val codeNameResolver: CodeNameResolver
        get() = message.fieldNameResolver

    val declarationResolver: DeclarationResolver
}
