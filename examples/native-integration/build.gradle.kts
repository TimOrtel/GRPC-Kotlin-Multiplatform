plugins {
    id("com.android.application") version "8.11.0" apply false
    id("com.android.library") version "8.11.0" apply false
    kotlin("android") version "2.2.20" apply false
    kotlin("plugin.compose") version "2.2.20" apply false
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.2.20"))
        classpath(kotlin("serialization", version = "2.2.20"))

        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.5")
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}