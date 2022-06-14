plugins {
    kotlin("js")
}

version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        useCommonJs()

        browser {
            webpackTask {
                output.libraryTarget = "commonjs2"
            }
        }

        binaries.executable()
    }
}

dependencies {
    implementation(project(":common"))

    api(npm("google-protobuf", "^3.19.1"))
    api(npm("grpc-web", "^1.3.0"))
    api(npm("protobufjs", "^6.11.2"))
}