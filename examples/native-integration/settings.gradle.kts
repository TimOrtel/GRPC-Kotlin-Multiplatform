rootProject.name = "grpc-multiplatform-example"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
    }
}

include("common")
include("protos")
include("js")
include("jvm")
include("android")