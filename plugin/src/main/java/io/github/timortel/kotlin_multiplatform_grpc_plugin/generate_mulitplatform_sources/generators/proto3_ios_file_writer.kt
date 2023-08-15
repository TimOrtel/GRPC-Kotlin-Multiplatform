package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ActualDslBuilder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.IosProtoFileWriter
import java.io.File

fun writeIOSFiles(protoFile: ProtoFile, iosOutputDir: File) {
    IosProtoFileWriter(protoFile).writeFile(iosOutputDir)

    //JVM helper
//    FileSpec
//        .builder(protoFile.pkg, protoFile.fileNameWithoutExtension + "_jvm_helper")
//        .apply {
//            JvmCommonFunctionGenerator(this).generateCommonGetter(protoFile.messages)
//        }
//        .build()
//        .writeTo(jvmOutputDir)

    writeDSLBuilder(protoFile, ActualDslBuilder, iosOutputDir)
}