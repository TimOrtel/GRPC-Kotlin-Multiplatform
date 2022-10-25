buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.10"))
        classpath(kotlin("serialization", version = "1.7.10"))

        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven(url = "https://jitpack.io")
    }
}