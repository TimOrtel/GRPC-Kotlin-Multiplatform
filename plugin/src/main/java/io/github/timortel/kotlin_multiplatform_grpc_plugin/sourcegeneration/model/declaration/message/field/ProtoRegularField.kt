package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.capitalize
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType

sealed class ProtoRegularField : ProtoBaseField() {
    abstract val type: ProtoType

    val isSetPropertyName: String get() = "is${name.capitalize()}Set"
}
