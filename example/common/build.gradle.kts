import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask

plugins {
    kotlin("multiplatform")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin")
}

group = "io.github.timortel.grpc_multiplaform.example.common"
version = "1.0-SNAPSHOT"

dependencies {
}

repositories {
    mavenLocal()
    mavenCentral()
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
                api("io.github.timortel:grpc-multiplatform-lib:0.1.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jvmMain by getting {
            dependencies {
                api(project(":generate-proto"))
                implementation("io.github.timortel:grpc-multiplatform-lib-jvm:0.1.0")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val jsMain by getting {
            dependencies {
                implementation("io.github.timortel:grpc-multiplatform-lib-js:0.1.0")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jsMain/kotlin").canonicalPath)
        }
    }
}

tasks.register<GenerateMultiplatformSourcesTask>("generateMPProtos") {
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}