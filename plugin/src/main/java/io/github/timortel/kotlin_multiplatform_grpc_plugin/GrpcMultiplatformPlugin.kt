package io.github.timortel.kotlin_multiplatform_grpc_plugin

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class GrpcMultiplatformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(KotlinMultiplatformPluginWrapper::class.java) {
            val extension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

            project.afterEvaluate {
                val generateMpProtosTask = project.tasks.withType(GenerateMultiplatformSourcesTask::class.java)
                val targets = extension.targets.toList()

                //JVM
                targets.filterIsInstance<KotlinJvmTarget>()
                    .flatMap { it.compilations }
                    .map { it.defaultSourceSet }
                    .forEach { kotlinSourceSet ->
                        project.tasks.withType(KotlinCompile::class.java).all { kotlinCompile ->
                            generateMpProtosTask.forEach { generateProtoTask ->
                                kotlinCompile.dependsOn(generateProtoTask)
                            }
                        }
                    }

                //JS
                targets
                    .filterIsInstance<KotlinJsIrTarget>()
                    .flatMap { it.compilations }
                    .map { it.defaultSourceSet }
                    .forEach { kotlinSourceSet ->
                        project.tasks.withType(Kotlin2JsCompile::class.java).all { kotlinCompile ->
                            generateMpProtosTask.forEach { generateProtoTask ->
                                kotlinCompile.dependsOn(generateProtoTask)
                            }
                        }
                    }
            }
        }
    }
}