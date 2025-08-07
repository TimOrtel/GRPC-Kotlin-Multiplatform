val libVersion = "1.0.0"

plugins {
    kotlin("multiplatform")
    id("com.android.library")

    id("io.github.timortel.kmpgrpc.plugin") version "1.0.0"
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

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64()
    ).forEach {
        it.binaries.framework {
            baseName = "Common"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
    }
}

kmpGrpc {
    common()
    jvm()
    android()
    js()
    native()

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
