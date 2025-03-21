package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.project

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.ProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.ProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoPackage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoProject
import java.io.File

abstract class ProtoProjectWriter {

    abstract val fileWriter: ProtoFileWriter
    abstract val serviceWriter: ProtoServiceWriter
    abstract val dslWriter: ProtoDslWriter

    fun writeProject(protoProject: ProtoProject, outputFolder: File) {
        writePackage(protoProject.rootPackage, outputFolder)
    }

    private fun writePackage(protoPackage: ProtoPackage, outputFolder: File) {
        protoPackage.files.forEach { file ->
            fileWriter.writeFile(file, outputFolder)
            serviceWriter.writeServiceStub(file, outputFolder)
            dslWriter.writeDslBuilderFile(file, outputFolder)
        }

        protoPackage.packages.forEach { pkg ->
            val subDir = outputFolder.resolve(pkg.name)
            subDir.mkdir()
            writePackage(pkg, subDir)
        }
    }
}