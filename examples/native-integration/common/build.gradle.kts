import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appleTargetsOnlyProperty = "appleTargetsOnly"
val appleTargetsOnly = if (project.hasProperty(appleTargetsOnlyProperty)) {
    project.property(appleTargetsOnlyProperty).toString() == "true"
} else false

plugins {
    kotlin("multiplatform")
    id("io.github.timortel.kmpgrpc.plugin") version "2.0.1"
    id("com.android.kotlin.multiplatform.library")
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

    if (!appleTargetsOnly) {
        jvm("jvm")
        android {
            compileSdk = 36
            namespace = "io.github.timortel.grpc_multiplaform.example.common"

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        js {
            useCommonJs()
            browser()
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
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
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
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
