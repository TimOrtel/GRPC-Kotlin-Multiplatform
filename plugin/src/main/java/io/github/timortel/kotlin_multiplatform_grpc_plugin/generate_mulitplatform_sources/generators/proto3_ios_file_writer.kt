package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ActualProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.IosProtoFileWriter
import java.io.File

fun writeIOSFiles(protoFile: ProtoFile, iosOutputDir: File) {
    IosProtoFileWriter(protoFile).writeFile(, iosOutputDir)

    writeDSLBuilder(protoFile, ActualProtoDslWriter, iosOutputDir)
}