buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {

    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}