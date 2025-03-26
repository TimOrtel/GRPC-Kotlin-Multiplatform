package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.project

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.ProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.ProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoPackage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoProject
import java.io.File

abstract class ProtoProjectWriter {

    abstract val fileWriter: ProtoFileWriter
    abstract val dslWriter: ProtoDslWriter

    fun writeProject(protoProject: ProtoProject, outputFolder: File) {
        writePackage(protoProject.rootPackage, outputFolder)
    }

    private fun writePackage(protoPackage: ProtoPackage, outputFolder: File) {
        protoPackage.files.forEach { file ->
            fileWriter.writeFiles(file, outputFolder)
            dslWriter.writeDslBuilderFile(file, outputFolder)
        }

        protoPackage.packages.forEach { pkg ->
            writePackage(pkg, outputFolder)
        }
    }
}
