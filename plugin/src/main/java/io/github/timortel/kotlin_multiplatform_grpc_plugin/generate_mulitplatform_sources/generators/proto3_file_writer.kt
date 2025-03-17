package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.CommonDslBuilder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.CommonProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import java.io.File

/**
 * Function called to add all proto files, common, js and jvm
 */
fun writeProtoFile(
    protoFile: ProtoFile,
    generateTarget: Map<String, Boolean>,
    commonOutputDir: File,
    jvmOutputDir: File,
    jsOutputDir: File,
    iosOutputDir: File
) {
    CommonProtoFileWriter(protoFile).writeFile(commonOutputDir)

    if (generateTarget[GrpcMultiplatformExtension.JS] == true) writeJsFiles(protoFile, jsOutputDir)
    if (generateTarget[GrpcMultiplatformExtension.JVM] == true) writeJvmFiles(protoFile, jvmOutputDir)
    if (generateTarget[GrpcMultiplatformExtension.IOS] == true) writeIOSFiles(protoFile, iosOutputDir)
    writeDSLBuilder(protoFile, CommonDslBuilder, commonOutputDir)
}

const val unrecognizedEnumField = "UNRECOGNIZED"