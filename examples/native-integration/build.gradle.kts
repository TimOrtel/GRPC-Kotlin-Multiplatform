plugins {
    id("com.android.application") version "8.7.0" apply false
    id("com.android.library") version "8.7.0" apply false
    kotlin("android") version "2.2.0" apply false
    kotlin("plugin.compose") version "2.2.0" apply false
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.2.0"))
        classpath(kotlin("serialization", version = "2.2.0"))

        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}