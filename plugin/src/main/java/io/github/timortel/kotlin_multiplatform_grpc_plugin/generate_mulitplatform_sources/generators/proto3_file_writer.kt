package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.CommonDslBuilder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.CommonProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import java.io.File

/**
 * Function called to add all proto files, common, js and jvm
 */
fun writeProtoFile(
    protoFile: ProtoFile,
    commonOutputDir: File,
    jvmOutputDir: File,
    jsOutputDir: File
) {
    CommonProtoFileWriter(protoFile).writeFile(commonOutputDir)

    writeJsFiles(protoFile, jsOutputDir)
    writeJvmFiles(protoFile, jvmOutputDir)
    writeDSLBuilder(protoFile, CommonDslBuilder, commonOutputDir)
}

const val unrecognizedEnumField = "UNRECOGNIZED"