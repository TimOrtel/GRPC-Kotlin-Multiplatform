@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("maven-publish")
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

repositories {
    mavenCentral()
    google()
}

kotlin {
    androidTarget("android") {
        publishLibraryVariants("release", "debug")
    }
    js(IR) {
        browser()
    }
    jvm("jvm")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "GRPC Kotlin Multiplatform Implementation"
        homepage = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"


        framework {
            baseName = "GRPCKotlinMultiplatform"
        }

        ios.deploymentTarget = "16.4"

        pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
        pod("Protobuf", version = "~> 3.21", moduleName = "Protobuf")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidJvmCommon by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(androidJvmCommon)
            dependencies {
                api(libs.google.protobuf.kotlin)
                api(libs.google.protobuf.java.util)
                api(libs.grpc.protobuf)
                api(libs.grpc.stub)
                api(libs.grpc.kotlin.stub)
            }
        }

        val androidMain by getting {
            dependsOn(androidJvmCommon)

            dependencies {
                api(libs.grpc.stub)
                api(libs.grpc.protobuf.lite)
                api(libs.grpc.kotlin.stub)
                api(libs.google.protobuf.kotlin.lite)
            }
        }

        val jsMain by getting {
            dependencies {
                api(npm("google-protobuf", "^3.19.1"))
                api(npm("grpc-web", "^1.3.0"))
                api(npm("protobufjs", "^6.11.2"))
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting

        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

android {
    namespace = "io.github.timortel.kotlin_multiplatform_grpc_lib"

    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}

kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}
