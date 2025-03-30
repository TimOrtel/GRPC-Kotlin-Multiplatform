package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile

sealed interface ProtoDeclParent {
    data class Message(val message: ProtoMessage) : ProtoDeclParent
    data class File(val file: ProtoFile) : ProtoDeclParent
}
