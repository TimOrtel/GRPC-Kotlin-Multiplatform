package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content

data class ProtoFile(
    val pkg: String,
    val fileNameWithoutExtension: String,
    val fileName: String,
    val messages: List<ProtoMessage>,
    val services: List<ProtoService>,
    val enums: List<ProtoEnum>
) {
    override fun toString(): String = "$fileName"
}