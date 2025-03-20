package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.enumeration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoDeclParent
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption

data class ProtoEnum(
    val name: String,
    val fields: List<ProtoEnumField>,
    val options: List<ProtoOption>
) {
    lateinit var parent: ProtoDeclParent

    init {
        fields.forEach { it.enum = this }
    }
}