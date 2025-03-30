rootProject.name = "grpc-kmp"

pluginManagement {
    includeBuild("kmp-grpc-plugin")
    includeBuild("pod-build-workaround")

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

include("kmp-grpc-core")
include("kmp-grpc-internal-test")
include("kmp-grpc-internal-test:test-server")
