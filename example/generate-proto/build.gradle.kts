import com.google.protobuf.gradle.*
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    kotlin("jvm")

    id("java")
    id("com.google.protobuf")
}

version = "unspecified"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    protobuf(project(":protos"))

    api("com.google.protobuf:protobuf-kotlin:${Versions.PROTOBUF}")
    api("com.google.protobuf:protobuf-java-util:${Versions.PROTOBUF}")
    api("io.grpc:grpc-protobuf:${Versions.GRPC}")
    api("io.grpc:grpc-stub:${Versions.GRPC}")
    api("io.grpc:grpc-kotlin-stub:${Versions.GRPC_KOTLIN}")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.PROTOBUF}"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.GRPC_KOTLIN}:jdk7@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
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