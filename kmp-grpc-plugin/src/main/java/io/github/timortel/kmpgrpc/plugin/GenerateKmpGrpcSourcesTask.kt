package io.github.timortel.kmpgrpc.plugin

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SystemInputFile
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateKmpGrpcSourcesTask : DefaultTask() {

    companion object {
        fun getOutputFolder(project: Project): File =
            project.layout.buildDirectory.dir("generated/source/kmp-grpc/").get().asFile

        fun getCommonOutputFolder(project: Project): File = getOutputFolder(project).resolve("commonMain/kotlin")
        fun getJVMOutputFolder(project: Project): File = getOutputFolder(project).resolve("jvmMain/kotlin")
        fun getJSOutputFolder(project: Project): File = getOutputFolder(project).resolve("jsMain/kotlin")
        fun getWasmJsOutputFolder(project: Project): File = getOutputFolder(project).resolve("wasmJsMain/kotlin")
        fun getIOSOutputFolder(project: Project): File = getOutputFolder(project).resolve("iosMain/kotlin")

        fun getWellKnownTypesFolder(project: Project): File =
            project.layout.buildDirectory.dir("well-known-protos").get().asFile
    }

    @get:InputFiles
    abstract val sourceFolders: ConfigurableFileCollection

    @get:Input
    abstract val targetSourcesMap: MapProperty<String, List<String>>

    @get:Input
    abstract val includeWellKnownTypes: Property<Boolean>

    @get:OutputDirectories
    val outputFolders: ConfigurableFileCollection = project.objects.fileCollection()

    init {
        outputFolders.setFrom(
            listOf(
                getCommonOutputFolder(project),
                getJVMOutputFolder(project),
                getJSOutputFolder(project),
                getIOSOutputFolder(project),
                getWasmJsOutputFolder(project)
            )
        )
    }

    @TaskAction
    fun generateSources() {
        val tsm = targetSourcesMap.get()

        val shouldGenerateTargetMap = KmpGrpcExtension.targets.associateWith { target ->
            tsm[target].orEmpty().isNotEmpty()
        }

        val outputFolder = getOutputFolder(project)
        outputFolder.mkdirs()

        val wellKnownTypeFolders = if (includeWellKnownTypes.get()) {
            listOf(SystemInputFile(getWellKnownTypesFolder(project)))
        } else emptyList()

        val protoFolders = sourceFolders.files.toList().map(::SystemInputFile) + wellKnownTypeFolders

        ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = protoFolders,
            shouldGenerateTargetMap = shouldGenerateTargetMap,
            commonOutputFolder = getCommonOutputFolder(project),
            jvmOutputFolder = getJVMOutputFolder(project),
            jsOutputFolder = getJSOutputFolder(project),
            wasmJsFolder = getWasmJsOutputFolder(project),
            iosOutputDir = getIOSOutputFolder(project)
        )

        val skipExtensionLibPropName = "io.github.timortel.kmp-grpc.internal.${project.name}.skip-wkt-ext"
        if (includeWellKnownTypes.get() && project.providers.gradleProperty(skipExtensionLibPropName).orNull != "true") {
            copyWellKnownExtensions()
        }
    }

    private fun copyWellKnownExtensions() {
        val path = "io/github/timortel/kmpgrpc/wkt/ext"
        val files = listOf("Any.kt", "Duration.kt", "Timestamp.kt", "Wrappers.kt")

        val outputDir = getCommonOutputFolder(project).resolve(path)
        outputDir.mkdirs()

        files.forEach { file ->
            GenerateKmpGrpcSourcesTask::class.java.classLoader.getResourceAsStream("$path/$file")?.use { input ->
                outputDir.resolve(file).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
