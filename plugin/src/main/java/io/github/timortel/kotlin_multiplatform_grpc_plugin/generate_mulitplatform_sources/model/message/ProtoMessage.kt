package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.enumeration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoDeclParent
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.field.ProtoMessageField

data class ProtoMessage(
    val name: String,
    val messages: List<ProtoMessage>,
    val enums: List<ProtoEnum>,
    val fields: List<ProtoMessageField>,
    val oneOfs: List<ProtoOneOf>,
    val mapFields: List<ProtoMapField>,
    val reservation: ProtoReservation,
    val options: List<ProtoOption>
) {
    lateinit var parent: ProtoDeclParent

    init {
        val parent = ProtoDeclParent.Message(this)

        messages.forEach { it.parent = parent }
        enums.forEach { it.parent = parent }

        oneOfs.forEach { it.message = this }
        fields.forEach { it.parent = this }
        mapFields.forEach { it.message = this }
    }
}
