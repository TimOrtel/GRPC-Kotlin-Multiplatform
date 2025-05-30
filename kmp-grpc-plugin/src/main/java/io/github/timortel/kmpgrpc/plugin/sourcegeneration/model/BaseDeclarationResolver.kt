package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType

interface BaseDeclarationResolver {
    /**
     * @return the declarations associated with this identifier, or an empty list of none have been found.
     */
    fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration?
}
