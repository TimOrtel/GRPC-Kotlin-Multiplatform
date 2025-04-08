import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")

    id("io.github.timortel.kmpgrpc.plugin") version libs.versions.grpcKotlinMultiplatform.get()
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

        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                    useChromeHeadless()
                }
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    cocoapods {
        summary = "GRPC Kotlin Multiplatform test library"
        homepage = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"
        ios.deploymentTarget = "14.1"
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":kmp-grpc-core"))
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
    namespace = "io.github.timortel.kmpgrpc.internal.test"

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

kmpGrpc {
    common()
    jvm()
    android()
    js()
    ios()

    includeWellKnownTypes = true

    protoSourceFolders = project.files("src/commonMain/proto/general", "src/commonMain/proto/unknownfield")
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
