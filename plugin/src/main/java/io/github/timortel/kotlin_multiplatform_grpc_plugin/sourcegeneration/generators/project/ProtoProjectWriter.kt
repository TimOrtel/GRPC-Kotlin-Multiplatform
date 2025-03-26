package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.dsl.ProtoDslWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.ProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoPackage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoProject
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
