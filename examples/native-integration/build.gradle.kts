plugins {
    id("com.android.application") version "9.1.1" apply false
    id("com.android.library") version "9.1.1" apply false
    kotlin("android") version "2.4.10" apply false
    kotlin("plugin.compose") version "2.4.10" apply false
    id("com.android.kotlin.multiplatform.library") version "9.1.1" apply false
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.4.10"))
        classpath(kotlin("serialization", version = "2.4.10"))

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