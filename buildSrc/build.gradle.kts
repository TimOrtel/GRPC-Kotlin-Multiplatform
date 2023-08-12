import com.google.protobuf.gradle.*

plugins {
    `kotlin-dsl`
    kotlin("jvm") version libs.versions.kotlin.get()

    id("java")
    id("com.google.protobuf") version libs.versions.protobufGradlePlugin.get()
}

repositories {
    gradlePluginPortal()
    google()
}

dependencies {
    // TODO: Replace in combination with patched podgen task
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20") {
        exclude("org.antlr")
    }
    implementation(libs.android.gradle.plugin) {
        exclude("org.antlr")
    }


    implementation(libs.google.protobuf.kotlin)
    implementation(libs.google.protobuf.java.util)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.kotlin.stub)

    implementation(libs.grpc.netty.shaded)

    implementation(libs.google.guava)
    implementation(libs.kotlinx.coroutines.core)
}

sourceSets {
    main {
        proto {
            srcDirs("../grpc-mp-test/src/commonMain/proto")
        }
        kotlin.srcDir(buildDir.resolve("generated/source/proto/main/grpc"))
        kotlin.srcDir(buildDir.resolve("generated/source/proto/main/grpckt"))
        kotlin.srcDir(buildDir.resolve("generated/source/proto/main/java"))
        kotlin.srcDir(buildDir.resolve("generated/source/proto/main/kotlin"))
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.48.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.1:jdk7@jar"
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
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}