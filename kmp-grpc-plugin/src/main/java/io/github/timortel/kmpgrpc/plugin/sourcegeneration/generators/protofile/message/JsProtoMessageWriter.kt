package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.JsProtoOneOfWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter

object JsProtoMessageWriter : ActualProtoMessageWriter() {

    override val protoOneOfWriter: ProtoOneOfWriter = JsProtoOneOfWriter

    override val target: SourceTarget = SourceTarget.Js
}
