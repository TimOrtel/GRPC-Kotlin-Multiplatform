plugins {
    kotlin("multiplatform")
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

    sourceSets {
        val jsMain by getting {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")

            dependencies {
                implementation(project(":app-common"))

                api(npm("google-protobuf", "^3.19.1"))
                api(npm("grpc-web", "^1.3.0"))
                api(npm("protobufjs", "^6.11.2"))
            }
        }
    }
}