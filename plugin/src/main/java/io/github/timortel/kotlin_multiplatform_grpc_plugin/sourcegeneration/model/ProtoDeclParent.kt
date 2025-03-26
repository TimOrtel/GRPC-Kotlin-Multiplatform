package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage

sealed interface ProtoDeclParent {
    data class Message(val message: ProtoMessage) : ProtoDeclParent
    data class File(val file: ProtoFile) : ProtoDeclParent
}
