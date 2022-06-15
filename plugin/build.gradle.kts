plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.18.0"
    antlr
}

group = "io.github.timortel"
version = "0.1.0"

java {
    withSourcesJar()
    withJavadocJar()
}

pluginBundle {
    website = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"
    vcsUrl = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform.git"
    tags = listOf("grpc", "protobuf", "kotlin-multiplatform", "kotlin", "multiplatform")
}

gradlePlugin {
    plugins {
        create("kotlin-multiplatform-grpc-plugin") {
            id = "io.github.timortel.kotlin-multiplatform-grpc-plugin"
            displayName = "GRPC Kotlin Multiplatform Plugin"
            description = "Plugin that generates Kotlin multiplatform wrapper classes for GRPC"

            implementationClass = "io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("maven") {
            from(project.components["java"])
            groupId = project.group as String
            version = project.version as String

            artifactId = "kotlin-multiplatform-grpc-plugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.antlr:antlr4:4.10.1")
    antlr("org.antlr:antlr4:4.10.1")

    implementation("com.squareup:kotlinpoet:1.11.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    dependsOn("generateGrammarSource")

    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.ExperimentalStdlibApi")
    }
}