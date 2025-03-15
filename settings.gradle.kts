rootProject.name = "kotlin-multiplatform-grpc-plugin"

pluginManagement {
    includeBuild("plugin")

    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        kotlin("jvm") version "2.1.10"
        kotlin("multiplatform") version "2.1.10"
        id("com.android.library")

        id("com.google.protobuf")
    }
}

include("grpc-multiplatform-lib")
include("grpc-mp-test")
