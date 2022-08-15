package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.ClassName

val kmMetadata = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "KMMetadata")
val kmStub = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.stub", "KMStub")
val kmChannel = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "KMChannel")
val kmAndroidJVMStub = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.stub", "AndroidJvmKMStub")
val kmTimeUnit = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.util", "TimeUnit")

val kmMessage = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "KMMessage")