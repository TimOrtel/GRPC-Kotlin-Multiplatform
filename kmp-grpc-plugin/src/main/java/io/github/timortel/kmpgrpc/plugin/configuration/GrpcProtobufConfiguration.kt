package io.github.timortel.kmpgrpc.plugin.configuration

import io.github.timortel.kmpgrpc.plugin.DownloadWellKnownTypesTask
import io.github.timortel.kmpgrpc.plugin.KmpGrpcExtension
import io.github.timortel.kmpgrpc.plugin.GenerateKmpGrpcSourcesTask
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

/**
 * Configures source generation and well known types download.
 */
object GrpcProtobufConfiguration {

    fun configure(project: Project) {
        val kmpGrpcExtension: KmpGrpcExtension =
            project.extensions.create("kmpGrpc", KmpGrpcExtension::class.java)

        configureSourceGeneration(project, kmpGrpcExtension)
        configureDownloadWellKnownTypes(project, kmpGrpcExtension)
    }

    private fun configureSourceGeneration(project: Project, kmpGrpcExtension: KmpGrpcExtension) {
        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        val generateSourcesTask = project.tasks.register("generateKmpGrpcSources", GenerateKmpGrpcSourcesTask::class.java) {
            it.sourceFolders.setFrom(kmpGrpcExtension.protoSourceFolders.from)
            it.targetSourcesMap.set(kmpGrpcExtension.targetSourcesMap.get().toMap())
            it.includeWellKnownTypes.set(kmpGrpcExtension.includeWellKnownTypes.get())
            it.internalVisibility.set(kmpGrpcExtension.internalVisibility.get())
            it.namingStrategy.set(kmpGrpcExtension.namingStrategy.get())
        }

        project.plugins.withType(KotlinMultiplatformPluginWrapper::class.java) {
            project.afterEvaluate {
                val generateKmpGrpcSourcesTasks = project.tasks.withType(GenerateKmpGrpcSourcesTask::class.java)

                val targetSourceMap = kmpGrpcExtension.targetSourcesMap.getOrElse(emptyMap())

                val baseGeneratedSourcesOutputFolder = GenerateKmpGrpcSourcesTask.getOutputFolder(project)

                targetSourceMap[KmpGrpcExtension.COMMON].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(
                        GenerateKmpGrpcSourcesTask.getCommonOutputFolder(
                            baseGeneratedSourcesOutputFolder
                        )
                    )
                }

                targetSourceMap[KmpGrpcExtension.JVM].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(
                        GenerateKmpGrpcSourcesTask.getJVMOutputFolder(
                            baseGeneratedSourcesOutputFolder
                        )
                    )
                }

                targetSourceMap[KmpGrpcExtension.JS].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(
                        GenerateKmpGrpcSourcesTask.getJSOutputFolder(
                            baseGeneratedSourcesOutputFolder
                        )
                    )
                }

                targetSourceMap[KmpGrpcExtension.WASMJS].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(
                        GenerateKmpGrpcSourcesTask.getWasmJsOutputFolder(
                            baseGeneratedSourcesOutputFolder
                        )
                    )
                }

                targetSourceMap[KmpGrpcExtension.NATIVE].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(
                        GenerateKmpGrpcSourcesTask.getNativeOutputFolder(
                            baseGeneratedSourcesOutputFolder
                        )
                    )
                }

                project.tasks.withType(KotlinCompileCommon::class.java).all { kotlinCompile ->
                    generateKmpGrpcSourcesTasks.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }

                //JVM
                project.tasks.withType(KotlinCompile::class.java).all { kotlinCompile ->
                    generateKmpGrpcSourcesTasks.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }

                //JS
                project.tasks.withType(Kotlin2JsCompile::class.java).all { kotlinCompile ->
                    generateKmpGrpcSourcesTasks.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }

                //IOS
                project.tasks.withType(KotlinNativeCompile::class.java).all { kotlinCompile ->
                    generateKmpGrpcSourcesTasks.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }
            }
        }

        project.afterEvaluate {
            project.tasks.withType(Jar::class.java).forEach { jarTask ->
                jarTask.dependsOn(generateSourcesTask)
            }
        }
    }

    private fun configureDownloadWellKnownTypes(project: Project, kmpGrpcExtension: KmpGrpcExtension) {
        val downloadTask =
            project.tasks.register("downloadProtoWellKnownTypes", DownloadWellKnownTypesTask::class.java) {
                it.outputDir.set(GenerateKmpGrpcSourcesTask.getWellKnownTypesFolder(project))
            }

        project.afterEvaluate {
            if (kmpGrpcExtension.includeWellKnownTypes.get()) {
                project.tasks.withType(GenerateKmpGrpcSourcesTask::class.java) {
                    it.dependsOn(downloadTask)
                }
            }
        }
    }
}
