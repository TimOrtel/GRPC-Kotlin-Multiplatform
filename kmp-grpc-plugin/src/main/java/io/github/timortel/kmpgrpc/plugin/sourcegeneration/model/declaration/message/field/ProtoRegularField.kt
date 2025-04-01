package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize

sealed class ProtoRegularField : ProtoBaseField() {
    abstract val type: ProtoType

    val isSetPropertyName: String get() = "is${name.capitalize()}Set"
}
