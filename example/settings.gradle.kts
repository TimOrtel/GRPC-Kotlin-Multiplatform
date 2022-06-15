rootProject.name = "grpc-multiplaform-example"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
    }

    val kotlinVersion = "1.6.10"

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("android") version kotlinVersion

        id("com.google.protobuf") version "0.8.17"
    }
}

include("common")
//include("protos")
//include("generate-proto")
//include("js")