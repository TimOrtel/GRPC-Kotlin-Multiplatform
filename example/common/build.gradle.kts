import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask

val libVersion = "0.2.2"

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.2.2"
}

group = "io.github.timortel.grpc_multiplaform.example.common"
version = "1.0-SNAPSHOT"


repositories {
    mavenLocal()
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
                api("io.github.timortel:grpc-multiplatform-lib:0.2.2")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jvmMain by getting {
            dependencies {
                api(project(":generate-proto"))
                api("io.github.timortel:grpc-multiplatform-lib-jvm:0.2.2")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val jsMain by getting {
            dependencies {
                api("io.github.timortel:grpc-multiplatform-lib-js:0.2.2")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jsMain/kotlin").canonicalPath)
        }
    }
}

tasks.register<GenerateMultiplatformSourcesTask>("generateMPProtos") {
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}