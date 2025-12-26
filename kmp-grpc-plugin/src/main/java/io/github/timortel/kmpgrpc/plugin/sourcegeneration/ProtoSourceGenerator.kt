package io.github.timortel.kmpgrpc.plugin.sourcegeneration

import com.squareup.kotlinpoet.FileSpec
import io.github.timortel.kmpgrpc.anltr.Protobuf2Lexer
import io.github.timortel.kmpgrpc.anltr.Protobuf2Parser
import io.github.timortel.kmpgrpc.anltr.Protobuf3Lexer
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kmpgrpc.anltr.ProtobufEditionsLexer
import io.github.timortel.kmpgrpc.anltr.ProtobufEditionsParser
import io.github.timortel.kmpgrpc.plugin.KmpGrpcExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.CommonProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.NativeProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.JsProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.project.JvmProtoProjectWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Visibility
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.parsing.ProtobufModelBuilderVisitor
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.slf4j.Logger
import java.io.File

object ProtoSourceGenerator {

    // regexes that allow for arbitrarily many whitespaces and comments in the proto file, but expect the syntax/edition statement first.
    private val protoSyntaxRegex =
        "^\\s*(?:(?://[^\\n]*|/\\*[\\s\\S]*?\\*/)\\s*|\\s*\\n)*syntax\\s*=\\s*[\"'](proto2|proto3)[\"']\\s*;[\\s\\S]*$".toRegex()
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
        val project = buildProtoProject(
            logger = logger,
            protoFolders = protoFolders,
            internalVisibility = internalVisibility
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

    internal fun buildProtoProject(
        logger: Logger,
        protoFolders: List<InputFile>,
        internalVisibility: Boolean
    ): ProtoProject {
        val folders = protoFolders.mapNotNull { sourceFolder ->
            walkFolder(sourceFolder, logger)
        }

        return ProtoProject(
            // All source folders are treated as if all of their files were in the same folder
            rootFolder = folders.fold(ProtoFolder("", emptyList(), emptyList())) { l, r ->
                ProtoFolder(name = "", folders = l.folders + r.folders, files = l.files + r.files)
            },
            logger = logger,
            defaultVisibility = if (internalVisibility) Visibility.INTERNAL else Visibility.PUBLIC
        )
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
        val fileText =
            file.inputStream().use { inputStream -> inputStream.reader().use { reader -> reader.readText() } }

        val languageVersion = when {
            fileText.matches(protoSyntaxRegex) -> {
                when (val versionGroup = protoSyntaxRegex.matchEntire(fileText)!!.groups[1]!!.value) {
                    "proto2" -> ProtoLanguageVersion.PROTO2
                    "proto3" -> ProtoLanguageVersion.PROTO3
                    else -> {
                        logger.warn("File $file uses unsupported proto language version $versionGroup. Only ${ProtoLanguageVersion.entries} are supported.")
                        return null
                    }
                }
            }

            fileText.matches(protoEditionsRegex) -> {
                when (val versionGroup = protoEditionsRegex.matchEntire(fileText)!!.groups[1]!!.value) {
                    "2023" -> ProtoLanguageVersion.EDITION2023
                    "2024" -> ProtoLanguageVersion.EDITION2024
                    else -> {
                        logger.warn("File $file uses unsupported proto editions $versionGroup. Only ${ProtoLanguageVersion.entries} are supported.")
                        return null
                    }
                }
            }

            else -> {
                logger.debug("Ignoring file {}, as it is not recognized as a valid proto file", file)
                return null
            }
        }

        data class Toolchain(
            val lexerFactory: (CharStream) -> Lexer,
            val visitProto: (ProtobufModelBuilderVisitor, TokenStream) -> ProtoFile
        )

        val toolchain = when (languageVersion) {
            ProtoLanguageVersion.PROTO2 -> Toolchain(
                lexerFactory = ::Protobuf2Lexer,
                visitProto = { visitor, tokenStream -> visitor.visitProto(Protobuf2Parser(tokenStream).proto()) }
            )

            ProtoLanguageVersion.PROTO3 -> Toolchain(
                lexerFactory = ::Protobuf3Lexer,
                visitProto = { visitor, tokenStream -> visitor.visitProto(Protobuf3Parser(tokenStream).proto()) }
            )

            ProtoLanguageVersion.EDITION2023, ProtoLanguageVersion.EDITION2024 -> Toolchain(
                lexerFactory = ::ProtobufEditionsLexer,
                visitProto = { visitor, tokenStream -> visitor.visitProto(ProtobufEditionsParser(tokenStream).proto()) }
            )
        }

        val visitor = ProtobufModelBuilderVisitor(
            filePath = file.path,
            fileNameWithoutExtension = file.nameWithoutExtension,
            fileName = file.name,
            protoLanguageVersion = languageVersion
        )

        val lexer = toolchain.lexerFactory(CharStreams.fromStream(file.inputStream()))
        return toolchain.visitProto(visitor, CommonTokenStream(lexer))
    }
}
