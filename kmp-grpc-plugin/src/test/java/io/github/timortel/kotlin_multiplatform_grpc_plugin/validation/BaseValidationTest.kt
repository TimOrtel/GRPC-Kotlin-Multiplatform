package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.mockk.spyk
import org.slf4j.LoggerFactory
import java.io.File

abstract class BaseValidationTest {
    val logger = spyk(LoggerFactory.getLogger("testlogger"))

    fun runGenerator(content: String) {
        val folder = createSingleFileProtoFolder(content)

        ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(folder),
            shouldGenerateTargetMap = emptyMap(),
            File(""),
            File(""),
            File(""),
            File(""),
            File("")
        )
    }
}
