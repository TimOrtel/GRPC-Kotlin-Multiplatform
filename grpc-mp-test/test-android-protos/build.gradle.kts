import com.google.protobuf.gradle.*

plugins {
    id("com.android.library")
    kotlin("android")

    id("com.google.protobuf")
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.grpc:grpc-stub:${Versions.JVM_GRPC_VERSION}")
    api("io.grpc:grpc-protobuf-lite:${Versions.JVM_GRPC_VERSION}")
    api("io.grpc:grpc-kotlin-stub:${Versions.JVM_GRPC_KOTLIN_VERSION}")
    api("com.google.protobuf:protobuf-kotlin-lite:${Versions.JVM_PROTOBUF_VERSION}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES_VERSION}")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.JVM_PROTOBUF_VERSION}"
    }

    plugins {
        id("java") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.JVM_GRPC_VERSION}"
        }

        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.JVM_GRPC_VERSION}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.JVM_GRPC_KOTLIN_VERSION}:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("java") {
                    option("lite")
                }
                id("grpc") {
                    option("lite")
                }
                id("grpckt") {
                    option("lite")
                }
            }

            it.builtins {
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            res.srcDirs("src/main/res")

            val protoSrcs = listOf(
                "../src/commonMain/proto"
            )
            withGroovyBuilder {
                "proto" {
                    "srcDir"(protoSrcs)
                }
            }
        }
    }
}