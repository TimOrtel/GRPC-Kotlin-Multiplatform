import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
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

    android {
        namespace = "io.github.timortel.kmpgrpc.internal.test"
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

kmpGrpc {
    common()
    jvm()
    android()
    js()
    wasmjs()
    native()

    includeWellKnownTypes = true

    protoSourceFolders = project.files(
        "src/commonMain/proto/general",
        "src/commonMain/proto/unknownfield",
        "src/commonMain/proto/editions",
        "src/commonMain/proto/proto2"
    )
}

buildConfig {
    packageName("io.github.timortel.kmpgrpc.internal.test")

    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    forClass("ServerCertificate") {
        val leafCertificateFile = projectDir.resolve("test-server/src/main/resources/standalone_leaf.pem")
        val caCertificateFile = projectDir.resolve("test-server/src/main/resources/ca.pem")
        val clientCertificateFile = projectDir.resolve("test-server/src/main/resources/client.pem")
        val clientKeyFile = projectDir.resolve("test-server/src/main/resources/client.key")

        val pemProvider = { file: File ->
            provider {
                "\"\"\"\n${file.readText()}\"\"\""
            }
        }

        buildConfigField("String", "STANDALONE_LEAF_CERTIFICATE", pemProvider(leafCertificateFile))
        buildConfigField("String", "CA_CERTIFICATE", pemProvider(caCertificateFile))
        buildConfigField("String", "CLIENT_CERTIFICATE", pemProvider(clientCertificateFile))
        buildConfigField("String", "CLIENT_KEY", pemProvider(clientKeyFile))

    }
}

tasks.withType(AbstractTestTask::class) {
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
