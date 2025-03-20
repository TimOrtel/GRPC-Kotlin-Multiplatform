package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.enumeration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service.ProtoService

data class ProtoFile(
    val `package`: String?,
    val fileName: String,
    val fileNameWithoutExtension: String,
    val messages: List<ProtoMessage>,
    val topLevelEnums: List<ProtoEnum>,
    val services: List<ProtoService>,
    val options: List<ProtoOption>,
    val imports: List<ProtoImport>
) {
    lateinit var folder: ProtoFolder

    init {
        val parent = ProtoDeclParent.File(this)

        services.forEach { it.file = this }
        messages.forEach { it.parent = parent }
        topLevelEnums.forEach { it.parent = parent }
    }
}
