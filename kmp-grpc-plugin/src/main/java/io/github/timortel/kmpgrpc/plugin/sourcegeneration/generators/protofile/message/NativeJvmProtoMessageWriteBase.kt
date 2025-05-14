package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.NativeJvmProtoOneOfWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter

abstract class NativeJvmProtoMessageWriteBase : ActualProtoMessageWriter() {

    override val protoOneOfWriter: ProtoOneOfWriter = NativeJvmProtoOneOfWriter
}
