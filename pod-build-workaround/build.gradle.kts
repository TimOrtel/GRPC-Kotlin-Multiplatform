plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("java-gradle-plugin")
}

group = "io.github.timortel"
version = "1.0"

gradlePlugin {
    plugins {
        create("pod-build-workaround") {
            id = "io.github.timortel.pod-build-workaround"
            implementationClass = "PodBuildWorkaroundPlugin"
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
}

repositories {
    mavenCentral()
    google()
}

sourceSets {
    main {
        java {
            srcDirs("shared-src/main/java")
        }
    }
}
