@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.linker

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("maven-publish")
    id("io.github.timortel.pod-build-workaround") version "1.0"
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
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
    }

    jvm("jvm")

//    iosX64()
    iosArm64()
    iosSimulatorArm64()

//    macosArm64()
//    macosX64()

    targets.filterIsInstance<KotlinNativeTarget>().forEach {
        println(it.targetName)
        it.compilations.getByName("main") {
            cinterops {
                create("kmp_grpc_native")
                create("kmp_grpc_native_rust")
            }
        }
    }

    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
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
                implementation(libs.kotlinx.io.core)
                implementation(libs.squareup.okio)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val iosJvmCommon by creating {
            dependsOn(commonMain.get())
        }

        val androidJvmCommon by creating {
            dependsOn(iosJvmCommon)

            dependencies {
                api(libs.grpc.stub)
                api(libs.grpc.protobuf.lite)
                api(libs.grpc.kotlin.stub)
            }
        }

        val iosJsCommon by creating {
            dependsOn(commonMain.get())
        }

        jvmMain {
            dependsOn(androidJvmCommon)

            dependencies {
                api(libs.grpc.api)
            }
        }

        androidMain {
            dependsOn(androidJvmCommon)
        }

        nativeMain {
            dependsOn(iosJsCommon)
            dependsOn(iosJvmCommon)
        }

        val jsTargetCommon by creating {
            dependsOn(iosJsCommon)
            dependsOn(commonMain.get())

            dependencies {
                implementation(libs.ktor.core)
            }
        }

        jsMain {
            dependsOn(jsTargetCommon)
        }

        wasmJsMain {
            dependsOn(jsTargetCommon)
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

android {
    namespace = "io.github.timortel.kmpgrpc.core"

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

tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    from(layout.projectDirectory.file("../THIRD_PARTY_LICENSES.txt")) {
        into("META-INF")
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
