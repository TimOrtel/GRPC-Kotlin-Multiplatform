allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

buildscript {
    dependencies {
        classpath(libs.antlr.runtime)
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
    dependsOn(":kmp-grpc-core:publishToMavenLocal")
}
