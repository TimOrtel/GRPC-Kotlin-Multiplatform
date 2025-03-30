package io.github.timortel.kmpgrpc.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.URL
import javax.inject.Inject

abstract class DownloadWellKnownTypesTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {

    companion object {
        private const val WELL_KNOWN_TYPES_RELATIVE_PATH = "google/protobuf"

        private const val WELL_KNOW_BASE_URL =
            "https://raw.githubusercontent.com/protocolbuffers/protobuf/refs/heads/main/src/google/protobuf"

        private val wellKnownTypes = listOf(
            "any.proto",
            "api.proto",
            "duration.proto",
            "empty.proto",
            "field_mask.proto",
            "source_context.proto",
            "struct.proto",
            "timestamp.proto",
            "type.proto",
            "wrappers.proto"
        )
    }

    @OutputDirectory
    val outputDir: DirectoryProperty = objectFactory.directoryProperty()

    @TaskAction
    fun run() {
        outputDir.dir(WELL_KNOWN_TYPES_RELATIVE_PATH).get().asFile.mkdirs()

        wellKnownTypes.forEach { protoFile ->
            val protoFileUrl = "$WELL_KNOW_BASE_URL/$protoFile"
            val url = URL(protoFileUrl)
            val outputFile = outputDir.file("$WELL_KNOWN_TYPES_RELATIVE_PATH/$protoFile").get().asFile

            logger.info("Downloading $protoFileUrl into ${outputFile.path}")

            url.openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
