package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.capitalize
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoType

sealed class ProtoRegularField : ProtoBaseField() {
    abstract val type: ProtoType

    val isSetPropertyName: String get() = "is${name.capitalize()}Set"
}
