@file:Suppress("UNUSED_VARIABLE")

import io.github.timortel.kmpgrpc.testserver.TestServer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension.OutputTarget
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version libs.versions.grpcKotlinMultiplatform.get()
}

version = "dev"

repositories {
    mavenCentral()
}

kotlin {
    androidTarget("android")

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        useCommonJs()

        nodejs()
        browser()
    }

    ios()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "GRPC Kotlin Multiplatform test library"
        homepage = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"
        ios.deploymentTarget = "14.1"

        pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
        pod("Protobuf")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":grpc-multiplatform-lib"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val iosMain by getting

        val jvmMain by getting {
            dependencies {
            }
        }

        val androidMain by getting {
            dependencies {
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val serializationTest by creating {
            dependsOn(commonMain)
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test"))
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jsTest by getting {
            dependsOn(serializationTest)
        }

        val iosTest by getting {
            dependsOn(serializationTest)
        }

        val jvmTest by getting {
            dependsOn(jvmMain)
            dependsOn(serializationTest)

            dependencies {
                runtimeOnly(libs.grpc.netty)
            }
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
    }
}

android {
    namespace = "io.github.timortel.kotlin_multiplatform_grpc_plugin.test"

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

grpcKotlinMultiplatform {
    targetSourcesMap.put(
        OutputTarget.COMMON,
        listOf(kotlin.sourceSets.getByName("commonMain"))
    )

    targetSourcesMap.put(
        OutputTarget.JS,
        listOf(kotlin.sourceSets.getByName("jsMain"), kotlin.sourceSets.getByName("jsTest"))
    )
    targetSourcesMap.put(
        OutputTarget.JVM,
        listOf(kotlin.sourceSets.getByName("androidMain"), kotlin.sourceSets.getByName("jvmMain"))
    )
    targetSourcesMap.put(
        OutputTarget.IOS,
        listOf(
            kotlin.sourceSets.getByName("iosMain"),
            kotlin.sourceSets.getByName("iosTest")
        )
    )

    val protoFolder = projectDir.resolve("src/commonMain/proto")
    protoSourceFolders.set(
        listOf(protoFolder)
    )
}

tasks.findByName("jvmTest")?.let {
    it.doFirst {
        TestServer.start()
    }

    it.doLast {
        TestServer.stop()
    }
}

tasks.named("iosSimulatorArm64Test", KotlinNativeSimulatorTest::class).configure {
    device = "iPhone 14"

    doFirst {
        TestServer.start()
    }

    doLast {
        TestServer.stop()
    }
}

tasks.withType(Test::class) {
    testLogging.setEvents(listOf(TestLogEvent.FAILED))

    testLogging.exceptionFormat = TestExceptionFormat.FULL
    testLogging.showExceptions = true
    testLogging.showCauses = true
    testLogging.showStackTraces = true
    testLogging.showStandardStreams = true

    reports.junitXml.required.set(true)
    reports.html.required.set(true)
    reports.junitXml.outputLocation.set(rootProject.rootDir.resolve("test-outputs/${project.name}/$name/"))
    reports.html.outputLocation.set(rootProject.rootDir.resolve("test-outputs/${project.name}/$name/"))
}