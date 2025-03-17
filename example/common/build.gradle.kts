import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension.OutputTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

val libVersion = "0.5.0"

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("native.cocoapods")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.5.0"
}

group = "io.github.timortel.grpc_multiplaform.example.common"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm("jvm")
    androidTarget()

    js(IR) {
        useCommonJs()
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
    }

    cocoapods {
        version = "1.0"
        summary = "gRPC KMP Example Common"
        homepage = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"

        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "common"
            isStatic = true

            transitiveExport = true
        }

        ios.deploymentTarget = "18.2"
    }
}

grpcKotlinMultiplatform {
    targetSourcesMap.put(
        OutputTarget.COMMON,
        listOf(kotlin.sourceSets.commonMain.get())
    )

    with(kotlin) {
        targetSourcesMap.put(
            OutputTarget.JS,
            listOf(kotlin.sourceSets.jsMain.get())
        )

        targetSourcesMap.put(
            OutputTarget.JVM,
            listOf(
                kotlin.sourceSets.androidMain.get(),
                kotlin.sourceSets.jvmMain.get()
            )
        )

        targetSourcesMap.put(
            OutputTarget.IOS,
            listOf(kotlin.sourceSets.iosMain.get())
        )
    }

    val protoFolder = projectDir.resolve("../protos/src/main/proto")
    protoSourceFolders.set(listOf(protoFolder))
}

android {
    compileSdk = 35
    namespace = "io.github.timortel.grpc_multiplaform.example.common"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
