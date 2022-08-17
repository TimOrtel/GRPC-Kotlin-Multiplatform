package io.github.timortel.kotlin_multiplatform_grpc_plugin

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.KotlinIosArm32Variant
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.KotlinIosArm64Variant
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.KotlinIosSimulatorArm64Variant
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.KotlinIosX64Variant
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.utils.`is`

class GrpcMultiplatformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val grpcMultiplatformExtension =
            project.extensions.create("grpcKotlinMultiplatform", GrpcMultiplatformExtension::class.java)

        project.plugins.withType(KotlinMultiplatformPluginWrapper::class.java) {
            val extension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

            project.afterEvaluate {
                val generateMpProtosTask = project.tasks.withType(GenerateMultiplatformSourcesTask::class.java)
                val targets = extension.targets.toList()

                //Common
                targets
                    .filter { it.platformType == KotlinPlatformType.common }
                    .flatMap { it.compilations }
                    .map { it.defaultSourceSet }
                    .forEach { kotlinSourceSet ->
                        kotlinSourceSet.kotlin.srcDir(GenerateMultiplatformSourcesTask.getCommonOutputFolder(project))
                    }

                val targetSourceMap = grpcMultiplatformExtension.targetSourcesMap.getOrElse(emptyMap()).orEmpty()

                println()

                targetSourceMap[GrpcMultiplatformExtension.OutputTarget.JVM].orEmpty().forEach {
                    it.kotlin.srcDir(GenerateMultiplatformSourcesTask.getJVMOutputFolder(project))
                }

                targetSourceMap[GrpcMultiplatformExtension.OutputTarget.JS].orEmpty().forEach {
                    it.kotlin.srcDir(GenerateMultiplatformSourcesTask.getJSOutputFolder(project))
                }
                targetSourceMap[GrpcMultiplatformExtension.OutputTarget.IOS].orEmpty().forEach {
                    it.kotlin.srcDir(GenerateMultiplatformSourcesTask.getIOSOutputFolder(project))
                }


                //JVM
                targets.filterIsInstance<KotlinJvmTarget>()
                    .flatMap { it.compilations }
                    .forEach { compilation ->
                        project.tasks.withType(KotlinCompile::class.java).all { kotlinCompile ->
                            generateMpProtosTask.forEach { generateProtoTask ->
                                kotlinCompile.dependsOn(generateProtoTask)
                            }
                        }

//                        if (compilation.name == KotlinCompilation.MAIN_COMPILATION_NAME) {
//                            compilation.defaultSourceSet.kotlin.srcDir(
//                                GenerateMultiplatformSourcesTask.getJVMOutputFolder(
//                                    project
//                                )
//                            )
//                        }
                    }

                //JS
                targets
                    .filterIsInstance<KotlinJsIrTarget>()
                    .flatMap { it.compilations }
                    .forEach { compilation ->
                        project.tasks.withType(Kotlin2JsCompile::class.java).all { kotlinCompile ->
                            generateMpProtosTask.forEach { generateProtoTask ->
                                kotlinCompile.dependsOn(generateProtoTask)
                            }
                        }

//                        if (compilation.name == KotlinCompilation.MAIN_COMPILATION_NAME) {
//                            compilation.defaultSourceSet.kotlin.srcDir(
//                                GenerateMultiplatformSourcesTask.getJSOutputFolder(
//                                    project
//                                )
//                            )
//                        }
                    }

                //IOS
                targets
                    .filterIsInstance<KotlinNativeTarget>()
                    .filter { it.konanTarget.family.isAppleFamily }
                    .flatMap { it.compilations }
                    .forEach { compilation ->
                        project.tasks.withType(KotlinNativeCompile::class.java).all { kotlinCompile ->
                            generateMpProtosTask.forEach { generateProtoTask ->
                                kotlinCompile.dependsOn(generateProtoTask)
                            }
                        }

//                        if (compilation.name == KotlinCompilation.MAIN_COMPILATION_NAME) {
//                            compilation.defaultSourceSet.kotlin.srcDir(
//                                GenerateMultiplatformSourcesTask.getIOSOutputFolder(
//                                    project
//                                )
//                            )
//                        }
                    }
            }
        }
    }
}