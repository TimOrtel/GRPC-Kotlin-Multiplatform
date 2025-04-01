package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.JvmIosProtoOneOfWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter

abstract class IosJvmProtoMessageWriteBase : ActualProtoMessageWriter() {

    override val protoOneOfWriter: ProtoOneOfWriter = JvmIosProtoOneOfWriter
}
