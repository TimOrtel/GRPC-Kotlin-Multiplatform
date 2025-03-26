package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.project

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ActualProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.IosProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.ProtoFileWriter

object IosProtoProjectWriter : ProtoProjectWriter() {
    override val fileWriter: ProtoFileWriter = IosProtoFileWriter
    override val dslWriter: ProtoDslWriter = ActualProtoDslWriter
}
