plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version libs.versions.gradlePluginPublish.get()
    id("com.github.gmazzo.buildconfig") version libs.versions.buildConfigPlugin.get()
    antlr
}

group = "io.github.timortel"
version = libs.versions.grpcKotlinMultiplatform.get()

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    website = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform"
    vcsUrl = "https://github.com/TimOrtel/GRPC-Kotlin-Multiplatform.git"

    plugins {
        create("kmp-grpc-plugin") {
            id = "io.github.timortel.kmpgrpc.plugin"
            displayName = "gRPC Kotlin Multiplatform Plugin"
            description = "Plugin that generates Kotlin multiplatform wrapper classes for gRPC"

            implementationClass = "io.github.timortel.kmpgrpc.plugin.KmpGrpcPlugin"
            tags = listOf("grpc", "kotlin", "kotlin-multiplatform")
        }
    }
}

kotlin {
    sourceSets.all {
        languageSettings {
            optIn("kotlin.RequiresOptIn")
            optIn("kotlin.ExperimentalStdlibApi")
        }
    }

    jvmToolchain(17)
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

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.mockk)
}

buildConfig {
    packageName("io.github.timortel.kmpgrpc.plugin")

    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("String", "VERSION", "\"${libs.versions.grpcKotlinMultiplatform.get()}\"")
}

publishing {
    repositories {
        mavenLocal()
    }
}

tasks.test.configure {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    dependsOn("generateGrammarSource")
}

tasks.withType<Jar>().all {
    dependsOn("generateGrammarSource")
}

tasks.withType<Javadoc> {
    exclude("**/Protobuf3Lexer.java")
    exclude("**/Protobuf3Parser.java")
    exclude("**/Protobuf3BaseVisitor.java")
    exclude("**/Protobuf3BaseListener.java")
}
