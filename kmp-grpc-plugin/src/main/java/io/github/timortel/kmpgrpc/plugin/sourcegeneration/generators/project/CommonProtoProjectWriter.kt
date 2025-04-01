package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.CommonProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.ProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.CommonProtoFileWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.ProtoFileWriter

object CommonProtoProjectWriter : ProtoProjectWriter() {
    override val fileWriter: ProtoFileWriter = CommonProtoFileWriter
    override val dslWriter: ProtoDslWriter = CommonProtoDslWriter
}
