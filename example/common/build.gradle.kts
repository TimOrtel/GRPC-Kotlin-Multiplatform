import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask

plugins {
    kotlin("multiplatform")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.1.0"
}

group = "io.github.timortel.grpc_multiplaform.example.common"
version = "1.0-SNAPSHOT"

dependencies {
    commonMainApi("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib:master-SNAPSHOT")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

kotlin {
    jvm("jvm")

    js(IR) {
        useCommonJs()

        nodejs()
        browser {
            webpackTask {
                output.libraryTarget = "commonjs2"
            }
        }

        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib:master-SNAPSHOT")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jvmMain by getting {
            dependencies {
                api(project(":generate-proto"))
                api("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib-jvm:master-SNAPSHOT")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val jsMain by getting {
            dependencies {
                implementation("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib-js:master-SNAPSHOT")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jsMain/kotlin").canonicalPath)
        }
    }
}

tasks.register<GenerateMultiplatformSourcesTask>("generateMPProtos") {
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}