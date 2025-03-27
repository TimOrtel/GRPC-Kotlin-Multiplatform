package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import io.github.timortel.kmpgrpc.anltr.Protobuf3Lexer
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.CommonProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.IosProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.JsProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.project.JvmProtoProjectWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.parsing.Protobuf3ModelBuilderVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.Logger
import java.io.File

object ProtoSourceGenerator {

    fun generateProtoFiles(
        logger: Logger,
        protoFolders: List<InputFile>,
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
            },
            logger = logger
        )

        // Before generating code, validate and print warnings / throw errors
        project.validate()

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
