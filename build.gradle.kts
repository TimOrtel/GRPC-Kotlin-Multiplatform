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

//Called by jitpack
tasks.register("publishToMavenLocal") {
    //No need to publish the plugin itself
    dependsOn(":grpc-multiplatform-lib:publishToMavenLocal")
}