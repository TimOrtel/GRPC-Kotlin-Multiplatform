package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoType

data class ProtoRpc(
    val name: String,
    val sendType: ProtoType.DefType,
    val returnType: ProtoType.DefType,
    val isSendingStream: Boolean,
    val isReceivingStream: Boolean,
    val options: List<ProtoOption>
) {
    lateinit var service: ProtoService
}
