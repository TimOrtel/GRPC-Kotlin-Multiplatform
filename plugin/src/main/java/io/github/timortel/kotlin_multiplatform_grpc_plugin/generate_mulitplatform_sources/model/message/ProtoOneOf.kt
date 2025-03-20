package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.field.ProtoOneOfField

data class ProtoOneOf(
    val name: String,
    val fields: List<ProtoOneOfField>,
    val options: List<ProtoOption>
) {
    lateinit var message: ProtoMessage

    init {
        fields.forEach { it.parent = this }
    }
}