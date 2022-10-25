import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask

val libVersion = "0.2.2"

plugins {
    kotlin("multiplatform")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.1.1"
}

group = "io.github.timortel.grpc_multiplaform.example.common"
version = "1.0-SNAPSHOT"

dependencies {
    commonMainApi("io.github.timortel:grpc-multiplatform-lib:$libVersion")
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

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("io.github.timortel:grpc-multiplatform-lib:$libVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jvmMain by getting {
            dependencies {
                api(project(":generate-proto"))
                api("io.github.timortel:grpc-multiplatform-lib-jvm:$libVersion")
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val jsMain by getting {
            dependencies {
                implementation("io.github.timortel:grpc-multiplatform-lib-js:$libVersion")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jsMain/kotlin").canonicalPath)
        }

        val iosArm64Main by getting {
            dependencies {
                implementation("io.github.timortel:grpc-multiplatform-lib-iosarm64:$libVersion")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/iosMain/kotlin").canonicalPath)
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation("io.github.timortel:grpc-multiplatform-lib-iossimulatorarm64:$libVersion")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/iosMain/kotlin").canonicalPath)
        }
        val iosX64Main by getting {
            dependencies {
                implementation("io.github.timortel:grpc-multiplatform-lib-iosx64:$libVersion")
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/iosMain/kotlin").canonicalPath)
        }
    }
}

tasks.register<GenerateMultiplatformSourcesTask>("generateMPProtos") {
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}