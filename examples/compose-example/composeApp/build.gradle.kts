@file:OptIn(ExperimentalComposeLibrary::class)

import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

val appleTargetsOnlyProperty = "appleTargetsOnly"
val appleTargetsOnly = if (project.hasProperty(appleTargetsOnlyProperty)) {
    project.property(appleTargetsOnlyProperty).toString() == "true"
} else false

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kmpGrpcPlugin)
}

if (!appleTargetsOnly) {
    plugins.apply("com.android.application")
}

group = "io.github.timortel.kmpgrpc.composeexample.composeapp"
version = "1.0-SNAPSHOT"

kotlin {
    applyDefaultHierarchyTemplate()

    if (!appleTargetsOnly) {
        androidTarget {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        jvm("desktop")

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName = "composeApp"
            browser {
                val rootDirPath = project.rootDir.path
                val projectDirPath = project.projectDir.path
                commonWebpackConfig {
                    outputFileName = "composeApp.js"
                    devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static = (static ?: mutableListOf()).apply {
                            // Serve sources to debug inside browser
                            add(rootDirPath)
                            add(projectDirPath)
                        }
                    }
                }
            }
            binaries.executable()
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries {
            framework {
                isStatic = true
                baseName = "composeApp"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kmp.grpc.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(compose.uiTest)
        }

        if (!appleTargetsOnly) {
            val desktopMain by getting

            androidMain.dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.io.grpc.okhttp)
            }

            desktopMain.dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.io.grpc.okhttp)
            }

            androidUnitTest {
                dependencies {
                    implementation(libs.androidx.test.junit)
                    implementation(libs.androidx.ui.test.manifest)
                    implementation(libs.androidx.test.core.ktx)
                    implementation(libs.junit)
                    implementation(libs.robolectric)
                }
            }
        }
    }
}

if (!appleTargetsOnly) {
    extensions.configure<ApplicationExtension>("android") {
        namespace = "io.github.timortel.kmpgrpc.composeexample"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            applicationId = "io.github.timortel.kmpgrpc.composeexample"
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }

    compose.desktop {
        application {
            mainClass = "io.github.timortel.kmpgrpc.composeexample.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "io.github.timortel.kmpgrpc.composeexample"
                packageVersion = "1.0.0"
            }
        }
    }
}

kmpGrpc {
    common()
    jvm(listOf("desktopMain"))
    wasmjs()
    android()
    native()

    protoSourceFolders = project.files("../proto")
}
