package io.github.timortel.kmpgrpc.plugin

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SystemInputFile
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
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

        ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = sourceFolders.files.toList().map(::SystemInputFile),
            shouldGenerateTargetMap = shouldGenerateTargetMap,
            commonOutputFolder = getCommonOutputFolder(project),
            jvmOutputFolder = getJVMOutputFolder(project),
            jsOutputFolder = getJSOutputFolder(project),
            iosOutputDir = getIOSOutputFolder(project)
        )
    }
}
