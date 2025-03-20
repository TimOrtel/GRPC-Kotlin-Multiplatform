package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.ProtoOneOf

data class ProtoOneOfField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>
) : ProtoRegularField() {
    lateinit var parent: ProtoOneOf
}