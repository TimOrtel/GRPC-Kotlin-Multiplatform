rootProject.name = "kotlin-multiplatform-grpc-plugin"

pluginManagement {
    includeBuild("plugin")

    repositories {
        gradlePluginPortal()
        google()
    }
    val kotlinVersion = "1.8.10"

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        id("com.android.library") version "7.4.2"

        id("com.google.protobuf") version "0.8.18" apply false
    }
}
//include("plugin")
include("grpc-multiplatform-lib")
include("grpc-mp-test:test-android-protos")
include("grpc-mp-test")
include("grpc-mp-test:test-jvm-protos")
