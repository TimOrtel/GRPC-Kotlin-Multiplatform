package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildPropertyNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

interface ProtoMessageProperty : ProtoChildProperty {

    /**
     * The parent of this property
     */
    val message: ProtoMessage

    override val resolvingParent: ProtoChildPropertyNameResolver
        get() = message
}
