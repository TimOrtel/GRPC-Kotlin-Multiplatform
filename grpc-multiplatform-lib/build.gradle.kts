plugins {
  id("com.android.library")
  kotlin("multiplatform")
  id("maven-publish")
}

group = "dev.baseio.grpc"
version = "0.2.2"

repositories {
  mavenCentral()
  google()
}

kotlin {
  android("android") {
    publishLibraryVariants("release", "debug")
  }
  js(IR) {
    browser()
    nodejs()
  }
  jvm("jvm")

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val GRPC = "1.49.1"
    val GRPC_KOTLIN = "1.3.0"
    val PROTOBUF = "3.21.6"

    val androidJvmCommon by creating {
      dependencies{
        implementation("com.google.protobuf:protobuf-kotlin:${PROTOBUF}")
      }
    }

    val jvmMain by getting {
      dependsOn(androidJvmCommon)
      dependencies {
        implementation("io.grpc:grpc-protobuf:${GRPC}")
        implementation("io.grpc:grpc-stub:${GRPC}")
        implementation("io.grpc:grpc-kotlin-stub:${GRPC_KOTLIN}")
      }
    }

    val androidMain by getting {
      dependsOn(androidJvmCommon)
      dependencies {
        implementation("io.grpc:grpc-stub:${GRPC}")
        implementation("io.grpc:grpc-protobuf:${GRPC}")
        implementation("io.grpc:grpc-kotlin-stub:${GRPC_KOTLIN}")
      }
    }

    val jsMain by getting {
      dependencies {
        api(npm("google-protobuf", "^3.19.1"))
        api(npm("grpc-web", "^1.3.0"))
        api(npm("protobufjs", "^6.11.2"))
      }
    }
  }
}

publishing {
  repositories {
    mavenLocal()
  }
}

android {
  compileSdk = 31

  defaultConfig {
    minSdk = 21
    targetSdk = 31
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  sourceSets {
    named("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
      res.srcDirs("src/androidMain/res")
    }
  }
}

kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
  binaries.all {
    binaryOptions["memoryModel"] = "experimental"
  }
}