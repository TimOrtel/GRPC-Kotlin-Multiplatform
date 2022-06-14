plugins {
    kotlin("jvm")
}

group = "de.ortel.grpc_multiplatform.jvm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("io.grpc:grpc-netty-shaded:${Versions.GRPC}")
}