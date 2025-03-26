package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.dsl.ActualProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.dsl.ProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.IosProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.ProtoFileWriter

object IosProtoProjectWriter : ProtoProjectWriter() {
    override val fileWriter: ProtoFileWriter = IosProtoFileWriter
    override val dslWriter: ProtoDslWriter = ActualProtoDslWriter
}
