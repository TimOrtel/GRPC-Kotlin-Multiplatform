plugins {
    id("com.android.library")
    kotlin("multiplatform")

    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

kotlin {
    android("android")

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        useCommonJs()

        browser()
    }

    sourceSets {
        val commonMain by getting {
//            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)

            dependencies {
                api(project(":grpc-multiplatform-lib"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test")) // This brings all the platform dependencies automatically
            }
        }

        val jvmMain by getting {
            dependencies {
                api(project("test-jvm-protos"))
            }

//            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)

        }

        val androidMain by getting {
            dependencies {
                api(project("test-android-protos"))
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }
    }
}

android {
    compileSdk = (31)

    defaultConfig {
        minSdk = (21)
        targetSdk = (31)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}

tasks.register<io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.GenerateMultiplatformSourcesTask>("generateMPProtos") {
    val protoFolder = projectDir.resolve("src/commonMain/proto")
    protoSourceFolders.set(
        listOf(protoFolder)
    )
}