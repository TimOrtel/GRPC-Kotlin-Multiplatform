package io.github.timortel.kmpgrpc.plugin

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty

abstract class GrpcMultiplatformExtension {

    companion object {
        internal const val COMMON = "common"
        internal const val JVM = "jvm"
        internal const val JS = "js"
        internal const val IOS = "ios"

        internal val targets = listOf(COMMON, JVM, JS, IOS)
    }

    /**
     * Maps the output target to the source sets that require it.
     */
    abstract val targetSourcesMap: MapProperty<String, List<String>>

    abstract val protoSourceFolders: ConfigurableFileCollection

    fun common(targets: List<String> = listOf("commonMain")) {
        targetSourcesMap.put(COMMON, targets)
    }

    fun jvm(targets: List<String> = listOf("jvmMain")) {
        targetSourcesMap.put(JVM, targetSourcesMap.get()[JVM].orEmpty() + targets)
    }

    fun android(targets: List<String> = listOf("androidMain")) {
        targetSourcesMap.put(JVM, targetSourcesMap.get()[JVM].orEmpty() + targets)
    }

    fun js(targets: List<String> = listOf("jsMain")) {
        targetSourcesMap.put(JS, targets)
    }

    fun ios(targets: List<String> = listOf("iosMain")) {
        targetSourcesMap.put(IOS, targets)
    }

    init {
        targetSourcesMap.convention(emptyMap())
    }
}

