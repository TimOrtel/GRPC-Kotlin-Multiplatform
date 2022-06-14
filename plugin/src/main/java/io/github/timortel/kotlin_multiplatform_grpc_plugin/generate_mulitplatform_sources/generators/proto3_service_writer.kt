package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.CommonServiceWriter
import java.io.File

fun writeServiceFile(protoFile: ProtoFile, commonOutputFolder: File, jvmOutputFolder: File, jsOutputFolder: File) {
    protoFile.services.forEach { service ->
        CommonServiceWriter.writeServiceStub(protoFile, service, commonOutputFolder)

        writeJsServiceFile(protoFile, service, jsOutputFolder)
        writeJvmServiceFile(protoFile, service, jvmOutputFolder)
    }
}