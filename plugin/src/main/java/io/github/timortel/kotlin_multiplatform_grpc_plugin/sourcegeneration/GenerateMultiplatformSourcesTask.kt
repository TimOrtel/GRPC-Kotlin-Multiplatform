package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import io.github.timortel.kmpgrpc.anltr.Protobuf3Lexer
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.CommonProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.IosProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.JsProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.JvmProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFolder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.parsing.Protobuf3ModelBuilderVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
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
    val folders = protoFolders.mapNotNull { sourceFolder ->
        walkFolder(sourceFolder)
    }

    val project = ProtoProject(
        // All source folders are treated as if all of their files were in the same folder
        rootFolder = folders.fold(ProtoFolder("", emptyList(), emptyList())) { l, r ->
            ProtoFolder(name = "", folders = l.folders + r.folders, files = l.files + r.files)
        }
    )

    if (shouldGenerateTargetMap[GrpcMultiplatformExtension.COMMON] == true) {
        CommonProtoProjectWriter.writeProject(project, commonOutputFolder)
    }

    if (shouldGenerateTargetMap[GrpcMultiplatformExtension.JVM] == true) {
        JvmProtoProjectWriter.writeProject(project, jvmOutputFolder)
    }

    if (shouldGenerateTargetMap[GrpcMultiplatformExtension.JS] == true) {
        JsProtoProjectWriter.writeProject(project, jsOutputFolder)
    }

    if (shouldGenerateTargetMap[GrpcMultiplatformExtension.IOS] == true) {
        IosProtoProjectWriter.writeProject(project, iosOutputDir)
    }
}

/**
 * @return the proto folder only if the folder or any of its subfolders did contain proto files
 */
private fun walkFolder(folder: File): ProtoFolder? {
    val folders = mutableListOf<ProtoFolder>()
    val files = mutableListOf<ProtoFile>()

    folder.listFiles()?.forEach { file ->
        when {
            file.isDirectory -> {
                val subFolder = walkFolder(file)
                if (subFolder != null) folders.add(subFolder)
            }

            file.isFile && file.extension == "proto" -> {
                val protoFile = readProtoFile(file)
                files.add(protoFile)
            }
        }
    }

    return if (folders.isNotEmpty() || files.isNotEmpty()) {
        ProtoFolder(name = folder.name, folders = folders, files = files)
    } else {
        null
    }
}

private fun readProtoFile(file: File): ProtoFile {
    val lexer = Protobuf3Lexer(CharStreams.fromStream(file.inputStream()))
    val parser = Protobuf3Parser(CommonTokenStream(lexer))

    val proto3File = parser.proto()

    return Protobuf3ModelBuilderVisitor(
        filePath = file.path,
        fileNameWithoutExtension = file.nameWithoutExtension,
        fileName = file.name
    ).visitProto(proto3File)
}
