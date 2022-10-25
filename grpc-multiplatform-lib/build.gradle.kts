plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("maven-publish")
}

group = "io.github.timortel"
version = "0.2.2"

repositories {
    mavenCentral()
    google()
}

kotlin {
    android("android") {
        publishLibraryVariants("release")
    }
    js(IR) {
        browser()
        nodejs()
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

        ios.deploymentTarget = "14.1"

        pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
        pod("Protobuf", version = "~> 3.21.7", moduleName = "Protobuf")
        //pod("gRPC-Core")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val GRPC = "1.49.1"
        val GRPC_KOTLIN = "1.3.0"
        val PROTOBUF = "3.21.6"

        val androidJvmCommon by creating {
            dependencies {
                implementation("com.google.protobuf:protobuf-kotlin:${PROTOBUF}")
            }
        }

        val jvmMain by getting {
            dependsOn(androidJvmCommon)
            dependencies {
                implementation("io.grpc:grpc-protobuf:${GRPC}")
                implementation("io.grpc:grpc-stub:${GRPC}")
                implementation("io.grpc:grpc-kotlin-stub:${GRPC_KOTLIN}")
            }
        }

        val androidMain by getting {
            dependsOn(androidJvmCommon)
            dependencies {
                implementation("io.grpc:grpc-stub:${GRPC}")
                implementation("io.grpc:grpc-protobuf:${GRPC}")
                implementation("io.grpc:grpc-kotlin-stub:${GRPC_KOTLIN}")
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
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
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

tasks.replace("podGenIOS", PatchedPodGenTask::class)