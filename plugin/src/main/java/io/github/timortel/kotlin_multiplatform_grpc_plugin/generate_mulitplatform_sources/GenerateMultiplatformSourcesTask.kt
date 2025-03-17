package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Lexer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Parser
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.writeProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.writeServiceFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.PacketTreeBuilder
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.slf4j.Logger
import java.io.File

@Suppress("LeakingThis")
abstract class GenerateMultiplatformSourcesTask : DefaultTask() {

    companion object {
        fun getOutputFolder(project: Project): File = project.buildDir.resolve("generated/source/kmp-grpc/")
        fun getCommonOutputFolder(project: Project): File = getOutputFolder(project).resolve("commonMain/kotlin")
        fun getJVMOutputFolder(project: Project): File = getOutputFolder(project).resolve("jvmMain/kotlin")
        fun getJSOutputFolder(project: Project): File = getOutputFolder(project).resolve("jsMain/kotlin")
        fun getIOSOutputFolder(project: Project): File = getOutputFolder(project).resolve("iosMain/kotlin")
    }

    init {
        val grpcMultiplatformExtension = project.extensions.findByType(GrpcMultiplatformExtension::class.java)
            ?: throw IllegalStateException("grpc multiplatform extension not specified.")

        val generateTarget = GrpcMultiplatformExtension.OutputTarget.entries.associateWith { target ->
            grpcMultiplatformExtension.targetSourcesMap.get()[target].orEmpty().isNotEmpty()
        }

        doLast {
            val sourceFolders = grpcMultiplatformExtension.protoSourceFolders.get()
            val outputFolder = getOutputFolder(project)
            outputFolder.mkdirs()

            generateProtoFiles(
                logger,
                sourceFolders,
                generateTarget,
                commonOutputFolder = getCommonOutputFolder(project),
                jvmOutputFolder = getJVMOutputFolder(project),
                jsOutputFolder = getJSOutputFolder(project),
                iosOutputDir = getIOSOutputFolder(project)
            )
        }
    }
}

private fun generateProtoFiles(
    log: Logger,
    protoFolders: List<File>,
    generateTarget: Map<GrpcMultiplatformExtension.OutputTarget, Boolean>,
    commonOutputFolder: File,
    jvmOutputFolder: File,
    jsOutputFolder: File,
    iosOutputDir: File
) {
    val sourceProtoFiles = protoFolders.map { sourceFolder ->
        sourceFolder
            .walk(FileWalkDirection.TOP_DOWN)
            .filter { it.isFile && it.extension == "proto" }
            .toList()
    }.flatten()

    val packetTree = PacketTreeBuilder.buildPacketTree(sourceProtoFiles)

    val protoFiles = sourceProtoFiles
        .map { protoFile ->
            log.debug("Generating KMP sources for proto file={}", protoFile)
            val lexer = Proto3Lexer(CharStreams.fromStream(protoFile.inputStream()))
            val parser = Proto3Parser(CommonTokenStream(lexer))

            val proto3File = parser.file()

            val javaUseMultipleFiles = proto3File.option()
                .any { it.optionName.text == "java_multiple_files" && it.optionValueExpression.text == "true" }

            val proto3FileBuilder =
                Proto3FileBuilder(protoFile.nameWithoutExtension, protoFile.name, packetTree, javaUseMultipleFiles)
            ParseTreeWalker().walk(
                proto3FileBuilder,
                proto3File
            )

            val result = proto3FileBuilder.protoFile
                ?: throw IllegalArgumentException("Failed generating protos for $protoFile because builder returned null.")

            result
        }

    protoFiles.forEach { protoFile ->
        writeProtoFile(protoFile, generateTarget, commonOutputFolder, jvmOutputFolder, jsOutputFolder, iosOutputDir)
        writeServiceFile(protoFile, generateTarget, commonOutputFolder, jvmOutputFolder, jsOutputFolder, iosOutputDir)
    }
}