package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Lexer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Parser
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.writeProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.writeServiceFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.PacketTreeBuilder
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.slf4j.Logger
import java.io.File

@Suppress("LeakingThis")
abstract class GenerateMultiplatformSourcesTask : DefaultTask() {

    @get:Input
    abstract val protoSourceFolders: ListProperty<File>

    init {
        protoSourceFolders.set(listOf(project.projectDir.resolve("src/main/proto")))

        doLast {
            val sourceFolders = protoSourceFolders.get()
            val outputFolder = project.buildDir.resolve("generated/source/kmp-grpc/")
            outputFolder.mkdirs()

            generateProtoFiles(
                logger,
                sourceFolders,
                commonOutputFolder = outputFolder.resolve("commonMain/kotlin"),
                jvmOutputFolder = outputFolder.resolve("jvmMain/kotlin"),
                jsOutputFolder = outputFolder.resolve("jsMain/kotlin")
            )
        }
    }
}

private fun generateProtoFiles(
    log: Logger,
    protoFolders: List<File>,
    commonOutputFolder: File,
    jvmOutputFolder: File,
    jsOutputFolder: File
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
            log.debug("Generating KMP sources for proto file=$protoFile")
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
        writeProtoFile(protoFile, commonOutputFolder, jvmOutputFolder, jsOutputFolder)
        writeServiceFile(protoFile, commonOutputFolder, jvmOutputFolder, jsOutputFolder)
    }
}