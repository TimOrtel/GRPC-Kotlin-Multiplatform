package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType

sealed class ProtoRegularField : ProtoBaseField() {
    abstract val type: ProtoType

    abstract val isPacked: Boolean

    override fun validate() {
        super.validate()

        type.validate()
    }
}
