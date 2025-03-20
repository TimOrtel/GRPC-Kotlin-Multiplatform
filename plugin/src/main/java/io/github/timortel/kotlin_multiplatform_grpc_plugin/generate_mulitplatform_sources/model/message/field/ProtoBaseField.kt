package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption

sealed class ProtoBaseField {
    abstract val name: String
    abstract val number: Int
    abstract val options: List<ProtoOption>
}