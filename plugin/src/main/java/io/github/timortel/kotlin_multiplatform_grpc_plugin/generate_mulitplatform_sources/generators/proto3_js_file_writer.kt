package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ActualProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.JsProtoFileWriter
import java.io.File

fun writeJsFiles(protoFile: ProtoFile, jsOutputDir: File) {
    JsProtoFileWriter(protoFile).writeFile(, jsOutputDir)

    writeDSLBuilder(protoFile, ActualProtoDslWriter, jsOutputDir)
}