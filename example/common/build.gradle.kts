import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension.OutputTarget

val libVersion = "0.5.0"

plugins {
    kotlin("multiplatform")
    id("com.android.library")

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
    jvm("jvm")
    androidTarget()

    js(IR) {
        useCommonJs()
        browser()
    }

    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("io.github.timortel:grpc-multiplatform-lib:$libVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
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
            listOf(kotlin.sourceSets.jsMain.get(), kotlin.sourceSets.jsTest.get())
        )

        targetSourcesMap.put(
            OutputTarget.JVM,
            listOf(kotlin.sourceSets.androidMain.get(), kotlin.sourceSets.jvmMain.get(), kotlin.sourceSets.jvmTest.get())
        )

        targetSourcesMap.put(
            OutputTarget.IOS,
            listOf(kotlin.sourceSets.iosMain.get(), kotlin.sourceSets.iosTest.get())
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
