import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.android.library")
    kotlin("multiplatform")

    id("io.github.timortel.kmpgrpc.plugin")
    alias(libs.plugins.buildConfig)
}

version = "dev"

repositories {
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    setupTargets(project)

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

        val nativeJvmTest by creating {
            dependsOn(commonTest.get())
        }

        val jsTestTargetCommon by creating {
            dependsOn(commonTest.get())

            dependencies {
                implementation(libs.ktor.core)
            }
        }

        val jvmMacOsTest by creating {
            dependsOn(commonTest.get())
        }

        jsTest {
            dependsOn(jsTestTargetCommon)
        }

        wasmJsTest {
            dependsOn(jsTestTargetCommon)
        }

        nativeTest {
            dependsOn(nativeJvmTest)
        }

        jvmTest {
            dependsOn(nativeJvmTest)
            dependsOn(jvmMacOsTest)

            dependencies {
                runtimeOnly(libs.grpc.netty)
            }
        }

        macosTest {
            dependsOn(jvmMacOsTest)
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
    wasmjs()
    native()

    includeWellKnownTypes = true

    protoSourceFolders = project.files("src/commonMain/proto/general", "src/commonMain/proto/unknownfield")
}

buildConfig {
    packageName("iio.github.timortel.kmpgrpc.internal.test")

    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    forClass("ServerCertificate") {
        val leafCertificateFile = projectDir.resolve("test-server/src/main/resources/standalone_leaf.pem")
        val caCertificateFile = projectDir.resolve("test-server/src/main/resources/ca.pem")

        buildConfigField("String", "STANDALONE_LEAF_CERTIFICATE", provider {
            "\"\"\"\n${leafCertificateFile.readText()}\"\"\""
        })
        buildConfigField("String", "CA_CERTIFICATE", provider {
            "\"\"\"\n${caCertificateFile.readText()}\"\"\""
        })
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

setupTestsTask()
