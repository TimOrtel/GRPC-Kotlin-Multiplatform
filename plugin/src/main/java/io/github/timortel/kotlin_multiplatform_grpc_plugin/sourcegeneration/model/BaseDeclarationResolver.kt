package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoDeclaration

interface BaseDeclarationResolver {
    /**
     * @return the declarations associated with this identifier, or an empty list of none have been found.
     */
    fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration?
}
