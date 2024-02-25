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

    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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
        all {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val iosJvmCommon by creating {
            dependsOn(commonMain.get())
        }

        val androidJvmCommon by creating {
            dependsOn(iosJvmCommon)
        }

        jvmMain {
            dependsOn(androidJvmCommon)
            dependencies {
                api(libs.grpc.stub)
                api(libs.grpc.protobuf.lite)
                api(libs.grpc.kotlin.stub)
            }
        }

        androidMain {
            dependsOn(androidJvmCommon)

            dependencies {
                api(libs.grpc.stub)
                api(libs.grpc.protobuf.lite)
                api(libs.grpc.kotlin.stub)
            }
        }

        jsMain {
            dependencies {
                api(npm("google-protobuf", "^3.19.1"))
                api(npm("grpc-web", "^1.3.0"))
                api(npm("protobufjs", "^6.11.2"))
            }
        }

        iosMain {
            dependsOn(commonMain.get())
            dependsOn(iosJvmCommon)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
