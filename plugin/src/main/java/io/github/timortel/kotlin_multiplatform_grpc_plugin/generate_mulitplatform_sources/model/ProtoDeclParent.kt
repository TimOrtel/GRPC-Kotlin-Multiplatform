package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage

sealed interface ProtoDeclParent {
    data class Message(val message: ProtoMessage) : ProtoDeclParent
    data class File(val file: ProtoFile) : ProtoDeclParent
}
