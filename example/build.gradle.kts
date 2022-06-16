buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.0"))
        classpath(kotlin("serialization", version = "1.7.0"))

        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}