import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")

    id("java")
    id("com.google.protobuf") version libs.versions.protobufGradlePlugin.get()
    application
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

repositories {
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin) {
        exclude("org.antlr")
    }
    implementation(libs.android.gradle.plugin) {
        exclude("org.antlr")
    }


    implementation(libs.google.protobuf.kotlin)
    implementation(libs.google.protobuf.java.util)
    implementation(libs.grpc.api)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.services)

    implementation(libs.grpc.netty.shaded)

    implementation(libs.google.guava)
    implementation(libs.kotlinx.coroutines.core)
}

sourceSets {
    main {
        proto {
            srcDirs("../src/commonMain/proto")
        }
        kotlin.srcDir(layout.buildDirectory.dir("generated/source/proto/main/grpc"))
        kotlin.srcDir(layout.buildDirectory.dir("generated/source/proto/main/grpckt"))
        kotlin.srcDir(layout.buildDirectory.dir("generated/source/proto/main/java"))
        kotlin.srcDir(layout.buildDirectory.dir("generated/source/proto/main/kotlin"))
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobufJvm.get()}"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpcJvm.get()}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${libs.versions.grpcKotlin.get()}:jdk8@jar"
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("io.github.timortel.kmpgrpc.testserver.MainKt")
}
