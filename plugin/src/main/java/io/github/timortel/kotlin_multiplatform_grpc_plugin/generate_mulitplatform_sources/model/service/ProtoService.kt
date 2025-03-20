package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption

data class ProtoService(
    val name: String,
    val rpcs: List<ProtoRpc>,
    val options: List<ProtoOption>
) {

    lateinit var file: ProtoFile

    init {
        rpcs.forEach { it.service = this }
    }
}
