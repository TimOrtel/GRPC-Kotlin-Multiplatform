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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import java.io.File

abstract class GenerateMultiplatformSourcesTask : DefaultTask() {

    companion object {
        fun getOutputFolder(project: Project): File =
            project.layout.buildDirectory.dir("generated/source/kmp-grpc/").get().asFile

        fun getCommonOutputFolder(project: Project): File = getOutputFolder(project).resolve("commonMain/kotlin")
        fun getJVMOutputFolder(project: Project): File = getOutputFolder(project).resolve("jvmMain/kotlin")
        fun getJSOutputFolder(project: Project): File = getOutputFolder(project).resolve("jsMain/kotlin")
        fun getIOSOutputFolder(project: Project): File = getOutputFolder(project).resolve("iosMain/kotlin")
    }

    @get:InputFiles
    abstract val sourceFolders: ConfigurableFileCollection

    @get:Input
    abstract val targetSourcesMap: MapProperty<String, List<String>>

    @get:OutputDirectories
    val outputFolders: ConfigurableFileCollection = project.objects.fileCollection()

    init {
        outputFolders.setFrom(
            listOf(
                getCommonOutputFolder(project),
                getJVMOutputFolder(project),
                getJSOutputFolder(project),
                getIOSOutputFolder(project)
            )
        )
    }

    @TaskAction
    fun generateSources() {
        val tsm = targetSourcesMap.get()

        val shouldGenerateTargetMap = GrpcMultiplatformExtension.targets.associateWith { target ->
            tsm[target].orEmpty().isNotEmpty()
        }

        val outputFolder = getOutputFolder(project)
        outputFolder.mkdirs()

        generateProtoFiles(
            log = logger,
            protoFolders = sourceFolders.files.toList(),
            shouldGenerateTargetMap = shouldGenerateTargetMap,
            commonOutputFolder = getCommonOutputFolder(project),
            jvmOutputFolder = getJVMOutputFolder(project),
            jsOutputFolder = getJSOutputFolder(project),
            iosOutputDir = getIOSOutputFolder(project)
        )
    }
}

private fun generateProtoFiles(
    log: Logger,
    protoFolders: List<File>,
    shouldGenerateTargetMap: Map<String, Boolean>,
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
        writeProtoFile(
            protoFile = protoFile,
            generateTarget = shouldGenerateTargetMap,
            commonOutputDir = commonOutputFolder,
            jvmOutputDir = jvmOutputFolder,
            jsOutputDir = jsOutputFolder,
            iosOutputDir = iosOutputDir
        )

        writeServiceFile(
            protoFile = protoFile,
            generateTarget = shouldGenerateTargetMap,
            commonOutputFolder = commonOutputFolder,
            jvmOutputFolder = jvmOutputFolder,
            jsOutputFolder = jsOutputFolder,
            iosOutputFolder = iosOutputDir
        )
    }
}