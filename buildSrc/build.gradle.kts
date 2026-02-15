plugins {
    `kotlin-dsl`
}

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
}
