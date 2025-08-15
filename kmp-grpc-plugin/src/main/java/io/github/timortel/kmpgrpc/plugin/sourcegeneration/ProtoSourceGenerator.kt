package io.github.timortel.kmpgrpc.plugin.sourcegeneration

import com.squareup.kotlinpoet.FileSpec
import io.github.timortel.kmpgrpc.anltr.Protobuf3Lexer
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kmpgrpc.plugin.KmpGrpcExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.CommonProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.NativeProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.JsProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.JvmProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.parsing.Protobuf3ModelBuilderVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.Logger
import java.io.File

object ProtoSourceGenerator {

    fun writeProtoFiles(
        logger: Logger,
        protoFolders: List<InputFile>,
        shouldGenerateTargetMap: Map<String, Boolean>,
        commonOutputFolder: File,
        jvmOutputFolder: File,
        jsOutputFolder: File,
        wasmJsFolder: File,
        nativeOutputDir: File
    ) {
        val shouldGenerateTargetMapBySourceTarget: Map<SourceTarget, Boolean> = mapOf(
            SourceTarget.Common to (shouldGenerateTargetMap[KmpGrpcExtension.COMMON] ?: false),
            SourceTarget.Jvm to (shouldGenerateTargetMap[KmpGrpcExtension.JVM] ?: false),
            SourceTarget.Native to (shouldGenerateTargetMap[KmpGrpcExtension.NATIVE] ?: false),
            SourceTarget.Js to (
                    (shouldGenerateTargetMap[KmpGrpcExtension.JS]
                        ?: false) || (shouldGenerateTargetMap[KmpGrpcExtension.WASMJS] ?: false)
                    ),
        )

        val fileMap = generateProtoFiles(logger, protoFolders, shouldGenerateTargetMapBySourceTarget)

        fileMap[SourceTarget.Common].writeTo(commonOutputFolder)
        fileMap[SourceTarget.Jvm].writeTo(jvmOutputFolder)
        fileMap[SourceTarget.Native].writeTo(nativeOutputDir)

        if (shouldGenerateTargetMap[KmpGrpcExtension.JS] == true) {
            fileMap[SourceTarget.Js].writeTo(jsOutputFolder)
        }

        if (shouldGenerateTargetMap[KmpGrpcExtension.WASMJS] == true) {
            fileMap[SourceTarget.Js].writeTo(wasmJsFolder)
        }
    }

    private fun List<FileSpec>?.writeTo(folder: File) {
        orEmpty().forEach { it.writeTo(folder) }
    }

    internal fun generateProtoFiles(
        logger: Logger,
        protoFolders: List<InputFile>,
        shouldGenerateTargetMap: Map<SourceTarget, Boolean>
    ): Map<SourceTarget, List<FileSpec>> {
        val folders = protoFolders.mapNotNull { sourceFolder ->
            walkFolder(sourceFolder)
        }

        val project = ProtoProject(
            // All source folders are treated as if all of their files were in the same folder
            rootFolder = folders.fold(ProtoFolder("", emptyList(), emptyList())) { l, r ->
                ProtoFolder(name = "", folders = l.folders + r.folders, files = l.files + r.files)
            },
            logger = logger
        )

        // Before generating code, validate and print warnings / throw errors
        project.validate()

        return buildMap {
            if (shouldGenerateTargetMap[SourceTarget.Common] == true) {
                put(SourceTarget.Common, CommonProtoProjectWriter.generateProjectFiles(project))
            }

            if (shouldGenerateTargetMap[SourceTarget.Jvm] == true) {
                put(SourceTarget.Jvm, JvmProtoProjectWriter.generateProjectFiles(project))
            }

            if (shouldGenerateTargetMap[SourceTarget.Js] == true) {
                put(SourceTarget.Js, JsProtoProjectWriter.generateProjectFiles(project))
            }

            if (shouldGenerateTargetMap[SourceTarget.Native] == true) {
                put(SourceTarget.Native, NativeProtoProjectWriter.generateProjectFiles(project))
            }
        }
    }

    /**
     * @return the proto folder only if the folder or any of its subfolders did contain proto files
     */
    private fun walkFolder(folder: InputFile): ProtoFolder? {
        val folders = mutableListOf<ProtoFolder>()
        val files = mutableListOf<ProtoFile>()

        folder.files.forEach { file ->
            when {
                file.isDirectory -> {
                    val subFolder = walkFolder(file)
                    if (subFolder != null) folders.add(subFolder)
                }

                file.isProtoFile -> {
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

    private fun readProtoFile(file: InputFile): ProtoFile {
        val lexer = Protobuf3Lexer(CharStreams.fromStream(file.inputStream()))
        val parser = Protobuf3Parser(CommonTokenStream(lexer))

        val proto3File = parser.proto()

        return Protobuf3ModelBuilderVisitor(
            filePath = file.path,
            fileNameWithoutExtension = file.nameWithoutExtension,
            fileName = file.name
        ).visitProto(proto3File)
    }
}
