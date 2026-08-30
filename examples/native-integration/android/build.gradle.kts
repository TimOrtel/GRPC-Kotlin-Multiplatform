import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

kotlin {
    jvmToolchain(17)

    android {
        compileSdk = 37
        namespace = "io.github.timortel.kmpgrpc.example.android"

        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }

        buildFeatures { compose = true }
    }
}

dependencies {
    implementation(project(":common"))

    implementation(platform("androidx.compose:compose-bom:2025.08.00"))

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    implementation("io.grpc:grpc-okhttp:1.74.0")
}