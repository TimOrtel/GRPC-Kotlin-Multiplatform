@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("maven-publish")
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

repositories {
    mavenCentral()
    google()
}

val compileNativeLibAsReleaseProperty = "io.github.timortel.kmp-grpc.internal.native.release"
val compileNativeLibAsRelease = if (project.hasProperty(compileNativeLibAsReleaseProperty)) {
    project.property(compileNativeLibAsReleaseProperty).toString() == "true"
} else false

kotlin {
    applyDefaultHierarchyTemplate()

    setupTargets(project)

    targets.filterIsInstance<KotlinNativeTarget>().forEach {
        it.compilations.getByName("main") {
            cinterops {
                create("kmp_grpc_native") {
                    definitionFile = if (compileNativeLibAsRelease) {
                        layout.projectDirectory.file("src/nativeInterop/cinterop/release/$name.def")
                    } else {
                        layout.projectDirectory.file("src/nativeInterop/cinterop/debug/$name.def")
                    }
                }
            }
        }
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

kotlin.targets.withType(KotlinNativeTarget::class.java) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}

val compileNativeCodeTask = tasks.register("compileNativeCode", Exec::class.java) {
    inputs.files(fileTree("../kmp-grpc-native/") {
        include("src/**/*.rs")
        include("Cargo.lock")
        include("Cargo.toml")
        include("compile.sh")
    })

    outputs.dir("../kmp-grpc-native/target")

    workingDir = project.layout.projectDirectory.dir("../kmp-grpc-native/").asFile

    commandLine = listOf("./compile.sh")
}

val genNativeHeadersTask = tasks.register("genNativeHeadersFromRust", Exec::class.java) {
    inputs.files(fileTree("../kmp-grpc-native/") {
        include("src/**/*.rs")
        include("genheaders.sh")
        include("cbindgen.toml")
    })

    outputs.dir("../kmp-grpc-native/include")

    workingDir = project.layout.projectDirectory.dir("../kmp-grpc-native/").asFile

    commandLine = listOf("./genheaders.sh")
}

tasks.withType<CInteropProcess>().configureEach {
    dependsOn(genNativeHeadersTask.get())
    dependsOn(compileNativeCodeTask.get())
}

val genNativeLicenseTextsTask = tasks.register("genNativeLicenseTexts", Exec::class.java) {
    inputs.files(fileTree("../kmp-grpc-native/") {
        include("about.hbs")
        include("about.toml")
        include("Cargo.toml")
        include("Cargo.lock")
        include("genlicensetexts.sh")
    })

    outputs.files(
        fileTree("../kmp-grpc-native/target") {
            include("**/*.html")
        }
    )

    workingDir = project.layout.projectDirectory.dir("../kmp-grpc-native/").asFile

    val profile = if (compileNativeLibAsRelease) "release" else "dev"
    val targetGroup = when (project.getTargetGroup()) {
        TargetGroup.ALL -> "all"
        TargetGroup.APPLE_TEST -> "apple_test"
        TargetGroup.OTHERS_TEST -> "other_test"
    }

    commandLine = listOf("./genlicensetexts.sh", profile, targetGroup)
}

val kotlinToRustTargets = mapOf(
    "linuxX64" to "x86_64-unknown-linux-gnu",
    "linuxArm64" to "aarch64-unknown-linux-gnu",

    "iosX64" to "x86_64-apple-ios",
    "iosArm64" to "aarch64-apple-ios",
    "iosSimulatorArm64" to "aarch64-apple-ios-sim",

    "macosArm64" to "aarch64-apple-darwin",
    "macosX64" to "x86_64-apple-darwin",

    "mingwX64" to "x86_64-pc-windows-gnu"
)

// Make sure the license texts are included
kotlin.targets.withType<KotlinNativeTarget>().configureEach {
    val zipTasks = tasks
        .withType<Zip>()
        .filter { zipTask -> zipTask.name.contains(name) && zipTask.name.contains("Klib") && zipTask.name.contains("kmp_grpc_native") }

    if (zipTasks.isEmpty())
        throw IllegalStateException("Could not inject license file into Klib. No corresponging zip task found for target $name. Has the gradle layout changed?")

    val rustTarget = kotlinToRustTargets[name]
        ?: throw IllegalStateException("Could not find corresponding rust target for native target $name.")

    zipTasks
        .forEach { zipTask ->
            zipTask.dependsOn(genNativeLicenseTextsTask.get())

            zipTask.from(layout.projectDirectory.file("../kmp-grpc-native/target/$rustTarget/THIRD_PARTY_LICENSES.html")) {
                into("META-INF")
            }
        }
}

tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    from(layout.projectDirectory.file("../THIRD_PARTY_LICENSES.txt")) {
        into("META-INF")
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
