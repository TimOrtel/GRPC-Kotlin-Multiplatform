allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

buildscript {
    dependencies {
        classpath("org.antlr:antlr4-runtime:4.10.1")
    }
}

//https://youtrack.jetbrains.com/issue/KT-46200
plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    id("com.android.library") apply false
}

tasks.register("publishToMavenLocal") {
    dependsOn("plugin:publishToMavenLocal")
    dependsOn(":grpc-multiplatform-lib:publishToMavenLocal")
}