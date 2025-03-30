import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

group = "io.github.timortel.kmpgrpc.example.jvm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    // For server only
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-protobuf:1.71.0")
    implementation("io.grpc:grpc-stub:1.71.0")
    implementation("io.grpc:grpc-netty-shaded:1.71.0")
    implementation("io.grpc:grpc-services:1.61.1")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.6")
}

sourceSets {
    main {
        proto {
            srcDirs("../protos/src/main/proto")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.6"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.71.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") {
                }
                id("grpckt") {
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