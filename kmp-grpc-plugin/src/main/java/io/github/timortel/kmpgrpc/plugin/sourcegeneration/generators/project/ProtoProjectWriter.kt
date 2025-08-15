package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project

import com.squareup.kotlinpoet.FileSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl.ProtoDslWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.ProtoFileWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoPackage

abstract class ProtoProjectWriter {

    abstract val fileWriter: ProtoFileWriter
    abstract val dslWriter: ProtoDslWriter

    fun generateProjectFiles(protoProject: ProtoProject): List<FileSpec> {
        return generatePackageFiles(protoProject.rootPackage)
    }

    private fun generatePackageFiles(protoPackage: ProtoPackage): List<FileSpec> {
        val packageFiles = protoPackage.files.flatMap { file ->
            fileWriter.generateFiles(file) + dslWriter.generateDslBuilderFile(file)
        }

        val subPackageFiles = protoPackage.packages.flatMap { pkg ->
            generatePackageFiles(pkg)
        }

        return packageFiles + subPackageFiles
    }
}
