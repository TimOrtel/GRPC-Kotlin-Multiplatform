rootProject.name = "kotlin-multiplatform-grpc-plugin"

pluginManagement {
    includeBuild("plugin")

    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        kotlin("jvm") version "1.9.0"
        kotlin("multiplatform") version "1.9.0"
        id("com.android.library")

        id("com.google.protobuf")
    }
}

include("grpc-multiplatform-lib")
include("grpc-mp-test")
