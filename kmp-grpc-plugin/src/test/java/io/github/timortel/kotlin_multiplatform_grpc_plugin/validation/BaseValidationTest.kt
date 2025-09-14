package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.InputFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.mockk.spyk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

abstract class BaseValidationTest {
    val logger: Logger = spyk(LoggerFactory.getLogger("testlogger"))

    fun runGenerator(content: String, protoVersion: ProtoVersion = ProtoVersion.PROTO3) {
        val folder = createSingleFileProtoFolder(protoVersion.header, content)

        runGenerator(listOf(folder))
    }

    fun runGenerator(folder: List<InputFile>) {
        ProtoSourceGenerator.writeProtoFiles(
            logger = logger,
            protoFolders = folder,
            shouldGenerateTargetMap = emptyMap(),
            internalVisibility = false,
            commonOutputFolder = File(""),
            jvmOutputFolder = File(""),
            jsOutputFolder = File(""),
            wasmJsFolder = File(""),
            nativeOutputDir = File("")
        )
    }

    enum class ProtoVersion(val header: String) {
        PROTO3("syntax = \"proto3\";"),
        EDITION2023("edition = \"2023\";")
    }
}
