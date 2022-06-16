import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask

val libVersion = "0.1.1"

plugins {
    kotlin("multiplatform")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.1.1"
}

group = "io.github.timortel.grpc_multiplaform.example.common"
version = "1.0-SNAPSHOT"

dependencies {
    commonMainApi("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib:$libVersion")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

kotlin {
    jvm("jvm")

    js(IR) {
        useCommonJs()
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib:$libVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jvmMain by getting {
            dependencies {
                api(project(":generate-proto"))
                api("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib-jvm:$libVersion")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val jsMain by getting {
            dependencies {
                implementation("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib-js:$libVersion")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jsMain/kotlin").canonicalPath)
        }
    }
}

tasks.register<GenerateMultiplatformSourcesTask>("generateMPProtos") {
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}