@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("maven-publish")
    alias(libs.plugins.buildConfig)
    signing
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain(17)

    applyDefaultHierarchyTemplate()

    setupTargets(project)

    targets.filterIsInstance<KotlinNativeTarget>().forEach {
        it.compilations.getByName("main") {
            cinterops {
                create("kmp_grpc_native") {
                    definitionFile = if (buildAsRelease) {
                        layout.projectDirectory.file("src/nativeInterop/cinterop/release/$name.def")
                    } else {
                        layout.projectDirectory.file("src/nativeInterop/cinterop/debug/$name.def")
                    }
                }
            }
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .flatMap { target -> target.compilations.toList().flatMap { it.allKotlinSourceSets } }
        .distinct()
        .filter { it != sourceSets.commonMain.get() && it != sourceSets.commonTest.get() }
        .forEach { sourceSet ->
            sourceSet.languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        all {
            languageSettings {
                optIn("io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi")
            }
        }

        commonMain {
            kotlin.srcDir(layout.projectDirectory.dir("../kmp-grpc-shared/src/commonMain"))

            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.io.core)
                implementation(libs.squareup.okio)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidJvmCommon by creating {
            dependsOn(commonMain.get())

            dependencies {
                api(libs.grpc.stub)
                api(libs.grpc.kotlin.stub)
            }
        }

        val nativeJsCommon by creating {
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
            dependsOn(nativeJsCommon)
        }

        val jsTargetCommon by creating {
            dependsOn(nativeJsCommon)
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

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            name = "BuildDir"
            url = uri(layout.buildDirectory.dir("repos/releases"))
        }
    }

    if (publications.findByName("jvm") != null) {
        publications.named<MavenPublication>("jvm") {
            artifact(emptyJavadocJar.get())
        }
    }

    publications.withType<MavenPublication>().all {
        pom {
            name.set("kmp-grpc-core")
            description.set("A gRPC core library for Kotlin Multiplatform")
            url.set("https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("timortel")
                    name.set("Tim Ortel")
                    email.set("100865202+TimOrtel@users.noreply.github.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/TimOrtel/GRPC-Kotlin-Multiplatform.git")
                developerConnection.set("scm:git:ssh://github.com/TimOrtel/GRPC-Kotlin-Multiplatform.git")
                url.set("https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform")
            }
        }
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

buildConfig {
    packageName("io.github.timortel.kmpgrpc.core")

    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("Boolean", "ENABLE_TRACE_LOGGING", "${!buildAsRelease}")
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

    val profile = if (buildAsRelease) "release" else "dev"
    val targetGroup = when (project.getTargetGroup()) {
        TargetGroup.ALL -> "all"
        TargetGroup.APPLE_TEST -> "apple_test"
        TargetGroup.OTHERS_TEST -> "other_test"
    }

    commandLine = listOf("./compile.sh", profile, targetGroup)
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

    commandLine = listOf("./genlicensetexts.sh")
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
    val zipRustKlibTasks = tasks
        .withType<Zip>()
        .filter { zipTask -> zipTask.name.contains(name) && zipTask.name.contains("Klib") && zipTask.name.contains("kmp_grpc_native") }

    val zipGeneralLibKlibTasks = tasks
        .withType<Zip>()
        .filter { zipTask -> zipTask.name == "${name}Klib" }


    if (zipRustKlibTasks.isEmpty())
        throw IllegalStateException("Could not inject license file into rust Klib. No corresponging zip task found for target $name. Has the gradle layout changed?")

    if (zipGeneralLibKlibTasks.isEmpty())
        throw IllegalStateException("Could not inject license file into general Klib. No corresponging zip task found for target $name. Has the gradle layout changed?")

    val rustTarget = kotlinToRustTargets[name]
        ?: throw IllegalStateException("Could not find corresponding rust target for native target $name.")

    zipRustKlibTasks
        .forEach { zipTask ->
            zipTask.dependsOn(genNativeLicenseTextsTask.get())

            zipTask.from(layout.projectDirectory.file("../kmp-grpc-native/target/$rustTarget/THIRD_PARTY_LICENSES.html")) {
                into("META-INF")
            }
        }

    zipGeneralLibKlibTasks
        .forEach { zipTask ->
            zipTask.from(layout.projectDirectory.file("../THIRD_PARTY_LICENSES.txt")) {
                into("META-INF")
            }
        }
}

tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    if (this == emptyJavadocJar.get()) return@configureEach

    from(layout.projectDirectory.file("../THIRD_PARTY_LICENSES.txt")) {
        into("META-INF")
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

if (buildAsRelease) {
    signing {
        sign(publishing.publications)
    }
}

setupTestsTask()

