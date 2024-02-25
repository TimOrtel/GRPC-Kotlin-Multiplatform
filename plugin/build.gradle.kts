plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version libs.versions.gradlePluginPublish.get()
    antlr
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

java {
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    website = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"
    vcsUrl = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform.git"

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
    implementation(libs.kotlinx.coroutines.core)
    antlr(libs.antlr)
    implementation(libs.antlr)

    implementation(libs.squareup.kotlinpoet)
    compileOnly(libs.kotlin.gradle.plugin)
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

tasks.withType<Jar>().all {
    dependsOn("generateGrammarSource")
}