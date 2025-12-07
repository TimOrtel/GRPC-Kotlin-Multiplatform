package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.InputFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolder
import io.mockk.spyk
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseValidationTest {
    val logger: Logger = spyk(LoggerFactory.getLogger("testlogger"))

    fun runGenerator(content: String, protoVersion: ProtoVersion = ProtoVersion.PROTO3) {
        val folder = createSingleFileProtoFolder(protoVersion.header, content)

        runGenerator(listOf(folder))
    }

    fun runGenerator(folder: List<InputFile>) {
        ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = folder,
            shouldGenerateTargetMap = mapOf(
                SourceTarget.Common to true,
                SourceTarget.Jvm to true,
                SourceTarget.Js to true,
                SourceTarget.Native to true
            ),
            internalVisibility = false
        )
    }

    enum class ProtoVersion(val header: String) {
        PROTO3("syntax = \"proto3\";"),
        EDITION2023("edition = \"2023\";"),
        EDITION2024("edition = \"2024\";")
    }
}
