buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.6.0"))

        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
        classpath("io.github.timortel:kotlin-multiplatform-grpc-plugin:0.1.0")
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}