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
                implementation(project(":common"))
            }
        }
    }
}

afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackCli.version = "4.10.0"
    }
}