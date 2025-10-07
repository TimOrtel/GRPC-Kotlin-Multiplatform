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

val grpcVersion = "1.75.0"
val grpcKotlinVersion = "1.4.3"
val protobufVersion = "3.25.6"

dependencies {
    implementation(project(":common"))

    // For server only
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-services:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
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
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
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