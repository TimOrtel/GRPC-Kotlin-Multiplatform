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

        browser()
    }

    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    cocoapods {
        summary = "GRPC Kotlin Multiplatform test library"
        homepage = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"
        ios.deploymentTarget = "14.1"

        pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
        pod("Protobuf")
    }

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
                api(project(":grpc-multiplatform-lib"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val serializationTest by creating {
            dependsOn(commonMain.get())
            dependsOn(commonTest.get())
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jsTest {
            dependsOn(serializationTest)
        }

        iosTest {
            dependsOn(serializationTest)
        }

        jvmTest {
            dependsOn(serializationTest)

            dependencies {
                runtimeOnly(libs.grpc.netty)
            }
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
        listOf(kotlin.sourceSets.commonMain.get())
    )

    with(kotlin) {
        targetSourcesMap.put(
            OutputTarget.JS,
            listOf(kotlin.sourceSets.jsMain.get(), kotlin.sourceSets.jsTest.get())
        )

        targetSourcesMap.put(
            OutputTarget.JVM,
            listOf(kotlin.sourceSets.androidMain.get(), kotlin.sourceSets.jvmMain.get(), kotlin.sourceSets.jvmTest.get())
        )

        targetSourcesMap.put(
            OutputTarget.IOS,
            listOf(kotlin.sourceSets.iosMain.get(), kotlin.sourceSets.iosTest.get())
        )
    }

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
    doFirst {
        //TestServer.start()
    }

    doLast {
        //TestServer.stop()
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

replacePodBuildWithCustomPodBuild()