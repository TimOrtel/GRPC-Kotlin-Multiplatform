package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

data class ProtoFolder(val folders: List<ProtoFolder>, val files: List<ProtoFile>) {
    /**
     * Only null on the root folder
     */
    var parent: ProtoFolder? = null

    init {
        folders.forEach { it.parent = this }
        files.forEach { it.folder = this }
    }
}
