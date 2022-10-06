import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension.OutputTarget

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.2.0"
}

version = "dev"

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
            dependencies {
                api(project(":grpc-multiplatform-lib"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api(project("test-jvm-protos"))
            }
        }

        val androidMain by getting {
            dependencies {
                api(project("test-android-protos"))
            }

            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/jvmMain/kotlin").canonicalPath)
        }

        val serializationTest by creating {
            dependsOn(commonMain)
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test"))
            }
            kotlin.srcDir(projectDir.resolve("build/generated/source/kmp-grpc/commonMain/kotlin").canonicalPath)
        }

        val jsTest by getting {
            dependsOn(serializationTest)
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

grpcKotlinMultiplatform {
    targetSourcesMap.put(
        OutputTarget.JS,
        listOf(kotlin.sourceSets.getByName("jsMain"), kotlin.sourceSets.getByName("jsTest"))
    )
    targetSourcesMap.put(
        OutputTarget.JVM,
        listOf(kotlin.sourceSets.getByName("androidMain"), kotlin.sourceSets.getByName("jvmMain"))
    )

    val protoFolder = projectDir.resolve("src/commonMain/proto")
    protoSourceFolders.set(
        listOf(protoFolder)
    )
}