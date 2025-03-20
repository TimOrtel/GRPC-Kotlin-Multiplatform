package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.enumeration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption

data class ProtoEnumField(
    val name: String,
    val number: Int,
    val options: List<ProtoOption>
) {
    lateinit var enum: ProtoEnum
}