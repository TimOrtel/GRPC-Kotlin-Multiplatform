package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.CommonServiceWriter
import java.io.File

fun writeServiceFile(
    protoFile: ProtoFile,
    generateTarget: Map<GrpcMultiplatformExtension.OutputTarget, Boolean>,
    commonOutputFolder: File,
    jvmOutputFolder: File,
    androidOutputFolder:File,
    jsOutputFolder: File,
    iosOutputFolder: File
) {
    protoFile.services.forEach { service ->
        CommonServiceWriter.writeServiceStub(protoFile, service, commonOutputFolder)

        if (generateTarget[GrpcMultiplatformExtension.OutputTarget.JS] == true)
            writeJsServiceFile(protoFile, service, jsOutputFolder)
        if (generateTarget[GrpcMultiplatformExtension.OutputTarget.JVM] == true)
            writeJvmServiceFile(protoFile, service, jvmOutputFolder)
        if (generateTarget[GrpcMultiplatformExtension.OutputTarget.Android] == true)
            writeJvmServiceFile(protoFile, service, androidOutputFolder)
        if (generateTarget[GrpcMultiplatformExtension.OutputTarget.IOS] == true) {
            writeIOSServiceFile(protoFile, service, iosOutputFolder)
        }
    }
}