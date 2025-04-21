plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("native.cocoapods")

    id("io.github.timortel.kmpgrpc.plugin") version "0.5.0"
}

group = "io.github.timortel.kmpgrpc.example.common"
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

kmpGrpc {
    common()
    jvm()
    android()
    js()
    ios()

    protoSourceFolders = project.files("../protos/src/main/proto")
}

android {
    compileSdk = 35
    namespace = "io.github.timortel.grpc_multiplaform.example.common"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
