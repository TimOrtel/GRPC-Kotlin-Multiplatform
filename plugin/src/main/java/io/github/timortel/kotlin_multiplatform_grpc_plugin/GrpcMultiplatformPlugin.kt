package io.github.timortel.kotlin_multiplatform_grpc_plugin

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

class GrpcMultiplatformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val grpcMultiplatformExtension =
            project.extensions.create("grpcKotlinMultiplatform", GrpcMultiplatformExtension::class.java)

        project.tasks.register("generateMPProtos", GenerateMultiplatformSourcesTask::class.java)

        project.plugins.withType(KotlinMultiplatformPluginWrapper::class.java) {

            project.afterEvaluate {
                val generateMpProtosTask = project.tasks.withType(GenerateMultiplatformSourcesTask::class.java)

                val targetSourceMap = grpcMultiplatformExtension.targetSourcesMap.getOrElse(emptyMap())

                targetSourceMap[GrpcMultiplatformExtension.OutputTarget.COMMON].orEmpty().forEach {
                    it.kotlin.srcDir(GenerateMultiplatformSourcesTask.getCommonOutputFolder(project))
                }

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