plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("maven-publish")
}

group = "io.github.timortel"
version = "0.1.0"

repositories {
    mavenCentral()
    google()
}

kotlin {
    android("android") {
        publishLibraryVariants("release", "debug")
    }
    js(IR) {
        browser()
        nodejs()
    }
    jvm("jvm")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val GRPC = "1.46.0"
        val GRPC_KOTLIN = "1.2.1"
        val PROTOBUF = "3.20.1"

        val androidJvmCommon by creating {

        }

        val jvmMain by getting {
            dependsOn(androidJvmCommon)
            dependencies {
                implementation("com.google.protobuf:protobuf-kotlin:${PROTOBUF}")
                implementation("com.google.protobuf:protobuf-java-util:${PROTOBUF}")
                implementation("io.grpc:grpc-protobuf:${GRPC}")
                implementation("io.grpc:grpc-stub:${GRPC}")
                implementation("io.grpc:grpc-kotlin-stub:${GRPC_KOTLIN}")
            }
        }

        val androidMain by getting {
            dependsOn(androidJvmCommon)

            dependencies {
                implementation("io.grpc:grpc-stub:${GRPC}")
                implementation("io.grpc:grpc-protobuf-lite:${GRPC}")
                implementation("io.grpc:grpc-kotlin-stub:${GRPC_KOTLIN}")
                implementation("com.google.protobuf:protobuf-kotlin-lite:${PROTOBUF}")
            }
        }

        val jsMain by getting {
            dependencies {
                api(npm("google-protobuf", "^3.19.1"))
                api(npm("grpc-web", "^1.3.0"))
                api(npm("protobufjs", "^6.11.2"))
            }
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

android {
    compileSdkVersion(31)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(31)
        versionCode = 1
        versionName = "1.0"
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