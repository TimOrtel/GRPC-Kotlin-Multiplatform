rootProject.name = "kotlin-multiplatform-grpc-plugin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }

    val kotlinVersion: String = "1.6.10"

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        id("com.android.library") version "4.2.2"
    }
}

include("plugin")
include("grpc-multiplatform-lib")
