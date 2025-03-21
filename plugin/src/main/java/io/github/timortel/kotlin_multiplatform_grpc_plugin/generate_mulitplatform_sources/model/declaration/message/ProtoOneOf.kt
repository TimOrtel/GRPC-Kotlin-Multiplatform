package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoOneOfField

data class ProtoOneOf(
    val name: String,
    val fields: List<ProtoOneOfField>,
    val options: List<ProtoOption>
) {
    lateinit var message: ProtoMessage

    val file: ProtoFile get() = message.file

    init {
        fields.forEach { it.parent = this }
    }
}
