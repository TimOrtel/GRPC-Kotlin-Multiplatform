package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.IosServiceWriter
import java.io.File

fun writeIOSServiceFile(protoFile: ProtoFile, service: ProtoService, iosOutputFolder: File) {
    IosServiceWriter.writeServiceStub(protoFile, service, iosOutputFolder)
}