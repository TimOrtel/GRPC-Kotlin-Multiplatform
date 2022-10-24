plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20") {
        exclude("org.antlr")
    }
    implementation("com.android.tools.build:gradle:7.0.4") {
        exclude("org.antlr")
    }
}