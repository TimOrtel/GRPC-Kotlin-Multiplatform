package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.ProtoMessage

data class ProtoMapField(
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    val keyType: ProtoType.MapKeyType,
    val valuesType: ProtoType
) : ProtoBaseField() {
    lateinit var message: ProtoMessage
}
