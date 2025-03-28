/**
 * This is a copy of https://github.com/JetBrains/kotlin/blob/6338f8bff2c6d5b79079035d8f54c583cd8d6722/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/native/cocoapods/tasks/PodBuildTask.kt *
 * This file also contains parts of https://github.com/JetBrains/kotlin/blob/6338f8bff2c6d5b79079035d8f54c583cd8d6722/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/native/cocoapods/KotlinCocoapodsPlugin.kt
 * The code has been adapted. License can be found at https://github.com/JetBrains/kotlin/blob/6338f8bff2c6d5b79079035d8f54c583cd8d6722/license/LICENSE.txt
 */

import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.CocoapodsDependency
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.CocoapodsDependency.PodLocation.Path
import org.jetbrains.kotlin.gradle.targets.native.tasks.CocoapodsTask
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildSettingsProperties.Companion.CONFIGURATION
import org.jetbrains.kotlin.gradle.targets.native.tasks.schemeName
import org.jetbrains.kotlin.konan.target.Family
import java.io.File
import java.util.*
import javax.inject.Inject

@DisableCachingByDefault
abstract class CustomPodBuildTask @Inject constructor(
    providerFactory: ProviderFactory,
    projectLayout: ProjectLayout,
    objectFactory: ObjectFactory,
) : CocoapodsTask() {

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val buildSettingsFile: RegularFileProperty

    @get:Nested
    abstract val pod: Property<CocoapodsDependency>

    @get:Input
    abstract val appleTarget: Property<String>

    @get:Input
    abstract val family: Property<Family>

    private val synthetic = projectLayout.cocoapodsBuildDirs.synthetic(family)

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    @get:InputDirectory
    val srcDir: Provider<Directory> = pod.flatMap { pod ->
        val podLocation = pod.source
        if (podLocation is Path) {
            projectLayout.dir(providerFactory.provider { podLocation.dir })
        } else {
            synthetic.map { it.dir("Pods/${pod.schemeName}") }
        }
    }

    @Suppress("unused") // declares an output
    @get:OutputFiles
    val buildResult: FileCollection = objectFactory.fileTree()
        .from(synthetic.map { it.dir("build") })
        .matching {
            it.include("**/${pod.get().schemeName}.*/")
            it.include("**/${pod.get().schemeName}/")
        }

    @get:Internal
    abstract val podsXcodeProjDir: DirectoryProperty

    @TaskAction
    fun buildDependencies() {
        val configuration = readSettingsFromFile(buildSettingsFile.get().asFile)

        val podsXcodeProjDir = podsXcodeProjDir.get()

        val podXcodeBuildCommand = listOf(
            "xcodebuild",
            "-project", podsXcodeProjDir.asFile.name,
            "-scheme", pod.get().schemeName,
            "-destination", "generic/platform=${appleTarget.get()}",
            "-configuration", configuration,
            "CLANG_CXX_LANGUAGE_STANDARD=c++17"
        )

        val cl = Thread.currentThread().contextClassLoader.loadClass("org.jetbrains.kotlin.gradle.utils.ProcessUtilsKt")
        val method = cl.declaredMethods.first { it.name == "runCommand" }
        method.isAccessible = true

        val processConfiguration: ProcessBuilder.() -> Unit = {
            directory(podsXcodeProjDir.asFile.parentFile)
            environment()
        }

        method.invoke(null, podXcodeBuildCommand, logger, null, processConfiguration)
    }
}

internal val ProjectLayout.cocoapodsBuildDirs: CocoapodsBuildDirs
    get() = CocoapodsBuildDirs(this)

internal class CocoapodsBuildDirs(private val layout: ProjectLayout) {

    val root: Provider<Directory>
        get() = layout.buildDirectory.dir("cocoapods")
    val framework: Provider<Directory>
        get() = dir("framework")
    val defs: Provider<Directory>
        get() = dir("defs")
    val publish: Provider<Directory>
        get() = dir("publish")

    fun synthetic(family: Provider<Family>): Provider<Directory> {
        return dir("synthetic").map { it.dir(family.get().platformLiteral) }
    }

    private fun dir(pathFromRoot: String): Provider<Directory> = root.map { it.dir(pathFromRoot) }
}

internal val Family.platformLiteral: String
    get() = when (this) {
        Family.OSX -> "macos"
        Family.IOS -> "ios"
        Family.TVOS -> "tvos"
        Family.WATCHOS -> "watchos"
        else -> throw IllegalArgumentException("Bad family ${this.name}")
    }

internal fun readSettingsFromFile(file: File): String {
    return file.reader().use { reader ->
        Properties().let {
            it.load(reader)
            it.getProperty(CONFIGURATION)!!
        }
    }
}
