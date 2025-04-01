rootProject.name = "grpc-kmp"

pluginManagement {
    includeBuild("kmp-grpc-plugin")
    includeBuild("pod-build-workaround")

    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        kotlin("jvm") version "2.1.20"
        kotlin("multiplatform") version "2.1.20"
        id("com.android.library")

        id("com.google.protobuf")
        id("io.github.timortel.kmpgrpc.plugin") version "0.5.0" apply false
        id("io.github.timortel.pod-build-workaround") version "1.0" apply false
    }
}

include("kmp-grpc-core")
include("kmp-grpc-wellknown-ext")
include("kmp-grpc-internal-test")
include("kmp-grpc-internal-test:test-server")
