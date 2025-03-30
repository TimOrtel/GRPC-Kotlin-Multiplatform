package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.ActualProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.ProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.JvmProtoFileWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.ProtoFileWriter

object JvmProtoProjectWriter : ProtoProjectWriter() {
    override val fileWriter: ProtoFileWriter = JvmProtoFileWriter
    override val dslWriter: ProtoDslWriter = ActualProtoDslWriter
}
