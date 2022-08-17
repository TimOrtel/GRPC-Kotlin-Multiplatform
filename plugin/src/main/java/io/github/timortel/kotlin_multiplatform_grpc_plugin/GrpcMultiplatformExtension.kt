package io.github.timortel.kotlin_multiplatform_grpc_plugin

import org.gradle.api.provider.MapProperty
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

abstract class GrpcMultiplatformExtension {

    /**
     * Maps the output target to the source sets that require it.
     */
    abstract val targetSourcesMap: MapProperty<OutputTarget, List<KotlinSourceSet>>

    enum class OutputTarget {
        JVM,
        JS,
        IOS
    }

    init {
        targetSourcesMap.convention(emptyMap())
    }
}