package io.github.timortel.kotlin_multiplatform_grpc_plugin

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

abstract class GrpcMultiplatformExtension {

    /**
     * Maps the output target to the source sets that require it.
     */
    abstract val targetSourcesMap: MapProperty<OutputTarget, List<KotlinSourceSet>>

    abstract val protoSourceFolders: ListProperty<File>

    enum class OutputTarget {
        JVM,
        JS,
        IOS
    }

    init {
        targetSourcesMap.convention(emptyMap())
        protoSourceFolders.convention(emptyList())
    }
}