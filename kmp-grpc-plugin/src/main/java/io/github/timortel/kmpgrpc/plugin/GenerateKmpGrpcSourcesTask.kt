package io.github.timortel.kmpgrpc.plugin

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SystemInputFile
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateKmpGrpcSourcesTask : DefaultTask() {

    companion object {
        fun getOutputFolder(project: Project): File =
            project.layout.buildDirectory.dir("generated/source/kmp-grpc/").get().asFile

        fun getCommonOutputFolder(outputFolder: File): File = outputFolder.resolve("commonMain/kotlin")
        fun getJVMOutputFolder(outputFolder: File): File = outputFolder.resolve("jvmMain/kotlin")
        fun getJSOutputFolder(outputFolder: File): File = outputFolder.resolve("jsMain/kotlin")
        fun getWasmJsOutputFolder(outputFolder: File): File = outputFolder.resolve("wasmJsMain/kotlin")
        fun getNativeOutputFolder(outputFolder: File): File = outputFolder.resolve("nativeMain/kotlin")

        fun getWellKnownTypesFolder(project: Project): File =
            project.layout.buildDirectory.dir("well-known-protos").get().asFile
    }

    @get:InputFiles
    abstract val sourceFolders: ConfigurableFileCollection

    @get:Input
    abstract val targetSourcesMap: MapProperty<String, List<String>>

    @get:Input
    abstract val includeWellKnownTypes: Property<Boolean>

    @get:Input
    abstract val internalVisibility: Property<Boolean>

    @get:Input
    abstract val skipWellKnownExtensions: Property<Boolean>

    @get:InputDirectory
    abstract val generatedSourcesOutputFolder: RegularFileProperty

    @get:InputDirectory
    abstract val wellKnownTypesFolder: RegularFileProperty

    @get:OutputDirectories
    val outputFolders: ConfigurableFileCollection = project.objects.fileCollection()

    init {
        val projectOutputFolder = getOutputFolder(project)
        generatedSourcesOutputFolder.set(projectOutputFolder)

        wellKnownTypesFolder.set(getWellKnownTypesFolder(project))

        outputFolders.setFrom(
            listOf(
                getCommonOutputFolder(projectOutputFolder),
                getJVMOutputFolder(projectOutputFolder),
                getJSOutputFolder(projectOutputFolder),
                getNativeOutputFolder(projectOutputFolder),
                getWasmJsOutputFolder(projectOutputFolder)
            )
        )

        val skipExtensionLibPropName = "io.github.timortel.kmp-grpc.internal.${project.name}.skip-wkt-ext"
        skipWellKnownExtensions.set(project.providers.gradleProperty(skipExtensionLibPropName).orNull != "true")
    }

    @TaskAction
    fun generateSources() {
        val tsm = targetSourcesMap.get()

        val shouldGenerateTargetMap = KmpGrpcExtension.targets.associateWith { target ->
            tsm[target].orEmpty().isNotEmpty()
        }

        val outputFolder = generatedSourcesOutputFolder.get().asFile
        outputFolder.mkdirs()

        val wellKnownTypeFolders = if (includeWellKnownTypes.get()) {
            listOf(SystemInputFile(wellKnownTypesFolder.get().asFile))
        } else emptyList()

        val protoFolders = sourceFolders.files.toList().map(::SystemInputFile) + wellKnownTypeFolders

        ProtoSourceGenerator.writeProtoFiles(
            logger = logger,
            protoFolders = protoFolders,
            shouldGenerateTargetMap = shouldGenerateTargetMap,
            commonOutputFolder = getCommonOutputFolder(outputFolder),
            jvmOutputFolder = getJVMOutputFolder(outputFolder),
            jsOutputFolder = getJSOutputFolder(outputFolder),
            wasmJsFolder = getWasmJsOutputFolder(outputFolder),
            nativeOutputDir = getNativeOutputFolder(outputFolder),
            internalVisibility = internalVisibility.get()
        )


        if (includeWellKnownTypes.get() && skipWellKnownExtensions.get()) {
            copyWellKnownExtensions()
        }
    }

    private fun copyWellKnownExtensions() {
        val path = "io/github/timortel/kmpgrpc/wkt/ext"
        val files = listOf("Any.kt", "Duration.kt", "Timestamp.kt", "Wrappers.kt")

        val outputDir = getCommonOutputFolder(generatedSourcesOutputFolder.get().asFile).resolve(path)
        outputDir.mkdirs()

        files.forEach { file ->
            val fileAsStream = GenerateKmpGrpcSourcesTask::class.java.classLoader.getResourceAsStream("$path/$file")
                ?: throw IllegalStateException("Could not find well-known-extension file $file in $path/$file")

            val text = fileAsStream.use { input ->
                input.reader().readText().let {
                    // A simple string replacement is sufficient for now
                    if (internalVisibility.get()) {
                        it.replace("public", "internal")
                    } else {
                        it
                    }
                }
            }

            outputDir.resolve(file).outputStream().use { output ->
                output.writer().use { writer ->
                    writer.write(text)
                }
            }
        }
    }
}
