package io.github.timortel.kotlin_multiplatform_grpc_plugin.configuration

import io.github.timortel.plugin.VERSION
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import replacePodBuildWithCustomPodBuildTask

object ProjectSetupConfiguration {
    fun configure(project: Project) {
        project.afterEvaluate {
            val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

            kotlinExtension.sourceSets.findByName("common")?.dependencies {
                implementation("io.github.timortel:grpc-multiplatform-lib:$VERSION")
            }

            val cocoapodsExtension = kotlinExtension.extensions.findByType(CocoapodsExtension::class.java)

            cocoapodsExtension?.apply {
                pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
                pod("Protobuf")
            }

            project.replacePodBuildWithCustomPodBuildTask()
        }
    }
}
