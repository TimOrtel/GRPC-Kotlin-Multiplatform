package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.ActualProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.ProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.NativeProtoFileWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.ProtoFileWriter

object NativeProtoProjectWriter : ProtoProjectWriter() {
    override val fileWriter: ProtoFileWriter = NativeProtoFileWriter
    override val dslWriter: ProtoDslWriter = ActualProtoDslWriter
}
