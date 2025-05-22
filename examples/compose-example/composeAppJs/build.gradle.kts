@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kmpGrpcPlugin)
}

group = "io.github.timortel.kmpgrpc.composeexample.composeappjs"
version = "1.0-SNAPSHOT"

kotlin {
    applyDefaultHierarchyTemplate()

    js(IR) {
        browser()
        binaries.executable()
    }


    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(compose.runtime)
        }

        jsTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(compose.html.testUtils)
        }
    }
}

kmpGrpc {
    common()
    js()

    protoSourceFolders = project.files("../proto")
}
