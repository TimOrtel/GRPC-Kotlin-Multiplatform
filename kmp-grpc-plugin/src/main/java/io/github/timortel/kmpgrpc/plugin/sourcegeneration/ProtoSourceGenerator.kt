package io.github.timortel.kmpgrpc.plugin.sourcegeneration

import com.squareup.kotlinpoet.FileSpec
import io.github.timortel.kmpgrpc.anltr.Protobuf3Lexer
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kmpgrpc.anltr.ProtobufEditionsLexer
import io.github.timortel.kmpgrpc.anltr.ProtobufEditionsParser
import io.github.timortel.kmpgrpc.plugin.KmpGrpcExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.CommonProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.NativeProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.JsProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.JvmProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Visibility
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.parsing.ProtobufModelBuilderVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.Logger
import java.io.File

object ProtoSourceGenerator {

    // regexes that allow for arbitrarily many whitespaces and comments in the proto file, but expect the syntax/edition statement first.
    private val proto3Regex =
        "^\\s*(?:(?://[^\\n]*|/\\*[\\s\\S]*?\\*/)\\s*|\\s*\\n)*syntax\\s*=\\s*[\"']proto3[\"']\\s*;[\\s\\S]*$".toRegex()
    private val protoEditionsRegex =
        "^\\s*(?:(?://[^\\n]*|/\\*[\\s\\S]*?\\*/)\\s*|\\s*\\n)*edition\\s*=\\s*[\"'](\\d+)[\"']\\s*;[\\s\\S]*$".toRegex()

    fun writeProtoFiles(
        logger: Logger,
        protoFolders: List<InputFile>,
        shouldGenerateTargetMap: Map<String, Boolean>,
        internalVisibility: Boolean,
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

        val fileMap = generateProtoFiles(
            logger = logger,
            protoFolders = protoFolders,
            shouldGenerateTargetMap = shouldGenerateTargetMapBySourceTarget,
            internalVisibility = internalVisibility
        )

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
        shouldGenerateTargetMap: Map<SourceTarget, Boolean>,
        internalVisibility: Boolean
    ): Map<SourceTarget, List<FileSpec>> {
        val folders = protoFolders.mapNotNull { sourceFolder ->
            walkFolder(sourceFolder, logger)
        }

        val project = ProtoProject(
            // All source folders are treated as if all of their files were in the same folder
            rootFolder = folders.fold(ProtoFolder("", emptyList(), emptyList())) { l, r ->
                ProtoFolder(name = "", folders = l.folders + r.folders, files = l.files + r.files)
            },
            logger = logger,
            defaultVisibility = if (internalVisibility) Visibility.INTERNAL else Visibility.PUBLIC
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
    private fun walkFolder(folder: InputFile, logger: Logger): ProtoFolder? {
        val folders = mutableListOf<ProtoFolder>()
        val files = mutableListOf<ProtoFile>()

        folder.files.forEach { file ->
            when {
                file.isDirectory -> {
                    val subFolder = walkFolder(file, logger)
                    if (subFolder != null) folders.add(subFolder)
                }

                file.isProtoFile -> {
                    val protoFile = readProtoFile(file, logger)
                    if (protoFile != null) {
                        files.add(protoFile)
                    }
                }
            }
        }

        return if (folders.isNotEmpty() || files.isNotEmpty()) {
            ProtoFolder(name = folder.name, folders = folders, files = files)
        } else {
            null
        }
    }

    private fun readProtoFile(file: InputFile, logger: Logger): ProtoFile? {
        val visitor = ProtobufModelBuilderVisitor(
            filePath = file.path,
            fileNameWithoutExtension = file.nameWithoutExtension,
            fileName = file.name
        )

        val fileText =
            file.inputStream().use { inputStream -> inputStream.reader().use { reader -> reader.readText() } }

        return when {
            fileText.matches(proto3Regex) -> {
                val proto3Lexer = Protobuf3Lexer(CharStreams.fromStream(file.inputStream()))
                val proto3Parser = Protobuf3Parser(CommonTokenStream(proto3Lexer))
                val proto3File = proto3Parser.proto()

                visitor.visitProto(proto3File)
            }

            fileText.matches(protoEditionsRegex) -> {
                val protoEditionsLexer = ProtobufEditionsLexer(CharStreams.fromStream(file.inputStream()))
                val protoEditionsParser = ProtobufEditionsParser(CommonTokenStream(protoEditionsLexer))
                val protoEditionsFile = protoEditionsParser.proto()

                visitor.visitProto(protoEditionsFile)
            }

            else -> {
                logger.warn("File $file does not seem to conform to any known proto syntax. Only proto3 and proto editions files are currently supported. Ignoring file.")
                null
            }
        }
    }
}
