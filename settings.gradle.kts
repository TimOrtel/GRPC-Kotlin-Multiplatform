rootProject.name = "grpc-kmp"

pluginManagement {
    includeBuild("kmp-grpc-plugin")

    repositories {
        gradlePluginPortal()
        google()
    }
}

include("kmp-grpc-core")
include("kmp-grpc-wellknown-ext")
include("kmp-grpc-internal-test")
include("kmp-grpc-internal-test:test-server")
