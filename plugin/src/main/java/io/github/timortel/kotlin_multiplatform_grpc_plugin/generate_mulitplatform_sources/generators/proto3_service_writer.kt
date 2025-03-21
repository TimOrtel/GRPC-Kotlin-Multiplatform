package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.CommonProtoServiceWriter
import java.io.File

fun writeServiceFile(
    protoFile: ProtoFile,
    generateTarget: Map<String, Boolean>,
    commonOutputFolder: File,
    jvmOutputFolder: File,
    jsOutputFolder: File,
    iosOutputFolder: File
) {
    protoFile.services.forEach { service ->
        CommonProtoServiceWriter.writeServiceStub(protoFile, service, commonOutputFolder)

        if (generateTarget[GrpcMultiplatformExtension.JS] == true)
            writeJsServiceFile(protoFile, service, jsOutputFolder)
        if (generateTarget[GrpcMultiplatformExtension.JVM] == true)
            writeJvmServiceFile(protoFile, service, jvmOutputFolder)
        if (generateTarget[GrpcMultiplatformExtension.IOS] == true) {
            writeIOSServiceFile(protoFile, service, iosOutputFolder)
        }
    }
}