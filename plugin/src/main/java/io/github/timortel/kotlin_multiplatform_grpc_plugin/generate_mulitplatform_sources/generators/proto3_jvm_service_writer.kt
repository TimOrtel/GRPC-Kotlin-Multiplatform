package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.JvmProtoServiceWriter
import java.io.File

fun writeJvmServiceFile(protoFile: ProtoFile, service: ProtoService, jvmOutputFolder: File) {
    JvmProtoServiceWriter.writeServiceStub(protoFile, service, jvmOutputFolder)
}