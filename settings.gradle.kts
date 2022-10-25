rootProject.name = "kotlin-multiplatform-grpc-plugin"

pluginManagement {
    includeBuild("plugin")

    repositories {
        gradlePluginPortal()
        google()
    }
    val kotlinVersion: String = "1.7.20"

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        id("com.android.library") version "7.0.4"

        id("com.google.protobuf") version "0.8.19" apply false
    }
}
//include("plugin")
include("grpc-multiplatform-lib")
