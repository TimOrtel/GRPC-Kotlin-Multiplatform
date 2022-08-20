package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

val kmMetadata = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "KMMetadata")
val kmStub = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.stub", "KMStub")
val kmChannel = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "KMChannel")
val kmAndroidJVMStub = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.stub", "AndroidJvmKMStub")
val kmTimeUnit = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.util", "TimeUnit")

val JSImpl = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "JSImpl")
val MessageDeserializer = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "MessageDeserializer")

val kmMessage = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "KMMessage")
val GPBCodedInputStreamWrapper =
    ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "GPBCodedInputStreamWrapper")

val writeKMMessage = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "writeKMMessage")
val readKMMessage = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "readKMMessage")
val computeMapSize = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "computeMapSize")
val computeMessageSize = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "computeMessageSize")

val writeMap = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "writeMap")
val readMapEntry = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "readMapEntry")