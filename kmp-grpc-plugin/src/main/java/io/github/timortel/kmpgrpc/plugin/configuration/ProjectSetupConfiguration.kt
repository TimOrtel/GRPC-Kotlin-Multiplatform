package io.github.timortel.kmpgrpc.plugin.configuration

import io.github.timortel.kmpgrpc.plugin.VERSION
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

object ProjectSetupConfiguration {
    fun configure(project: Project) {
        project.afterEvaluate {
            val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

            kotlinExtension.sourceSets.findByName("commonMain")?.dependencies {
                if (project.providers.gradleProperty("io.github.timortel.kmp-grpc.internal").orNull != "true") {
                    api("io.github.timortel:kmp-grpc-core:$VERSION")
                }
            }

            kotlinExtension
                .targets
                .filterIsInstance<KotlinNativeTarget>()
                .flatMap { it.compilations }
                .flatMap { it.allKotlinSourceSets }
                .filter { it.name != "commonMain" }
                .forEach {
                    it.languageSettings {
                        optIn("kotlinx.cinterop.ExperimentalForeignApi")
                    }
                }

            kotlinExtension.compilerOptions {
                freeCompilerArgs.set(
                    freeCompilerArgs.get() + listOf("-Xexpect-actual-classes")
                )
            }
        }
    }
}
