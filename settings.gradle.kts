rootProject.name = "kotlin-multiplatform-grpc-plugin"

pluginManagement {
    includeBuild("plugin")

    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        kotlin("jvm")
        kotlin("multiplatform")
        id("com.android.library")

        id("com.google.protobuf")
    }
}

include("grpc-multiplatform-lib")
include("grpc-mp-test")
