import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
    kotlin("multiplatform")
    id("maven-publish")

    id("io.github.timortel.kmpgrpc.plugin")
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

repositories {
    mavenCentral()
    google()
}

kotlin {
    applyDefaultHierarchyTemplate()
    explicitApi()

    setupTargets(project)

    android {
        namespace = "io.github.timortel.kmpgrpc.wkt.ext"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":kmp-grpc-core"))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

kmpGrpc {
    common()
    jvm()
    android()
    js()
    native()

    includeWellKnownTypes = true

    protoSourceFolders.from(project.layout.projectDirectory.dir("src/commonTest/proto"))
}
