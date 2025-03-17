package io.github.timortel.kotlin_multiplatform_grpc_plugin.configuration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

object GrpcProtobufConfiguration {
    fun configure(project: Project) {
        val grpcMultiplatformExtension =
            project.extensions.create("grpcKotlinMultiplatform", GrpcMultiplatformExtension::class.java)

        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        project.tasks.register("generateMPProtos", GenerateMultiplatformSourcesTask::class.java) {
            it.sourceFolders.setFrom(grpcMultiplatformExtension.protoSourceFolders.from)
            it.targetSourcesMap.set(grpcMultiplatformExtension.targetSourcesMap.get().toMap())
        }

        project.plugins.withType(KotlinMultiplatformPluginWrapper::class.java) {
            project.afterEvaluate {
                val generateMpProtosTask = project.tasks.withType(GenerateMultiplatformSourcesTask::class.java)

                val targetSourceMap = grpcMultiplatformExtension.targetSourcesMap.getOrElse(emptyMap())

                targetSourceMap[GrpcMultiplatformExtension.COMMON].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(GenerateMultiplatformSourcesTask.getCommonOutputFolder(project))
                }

                targetSourceMap[GrpcMultiplatformExtension.JVM].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(GenerateMultiplatformSourcesTask.getJVMOutputFolder(project))
                }

                targetSourceMap[GrpcMultiplatformExtension.JS].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(GenerateMultiplatformSourcesTask.getJSOutputFolder(project))
                }

                targetSourceMap[GrpcMultiplatformExtension.IOS].orEmpty().forEach {
                    kotlinExtension.sourceSets.findByName(it)?.kotlin?.srcDir(GenerateMultiplatformSourcesTask.getIOSOutputFolder(project))
                }

                project.tasks.withType(KotlinCompileCommon::class.java).all { kotlinCompile ->
                    generateMpProtosTask.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }

                //JVM
                project.tasks.withType(KotlinCompile::class.java).all { kotlinCompile ->
                    generateMpProtosTask.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }

                //JS
                project.tasks.withType(Kotlin2JsCompile::class.java).all { kotlinCompile ->
                    generateMpProtosTask.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }

                //IOS
                project.tasks.withType(KotlinNativeCompile::class.java).all { kotlinCompile ->
                    generateMpProtosTask.forEach { generateProtoTask ->
                        kotlinCompile.dependsOn(generateProtoTask)
                    }
                }
            }
        }
    }
}
