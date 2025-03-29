package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants

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
val kmEnum = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.message", "KmEnum")

val writeKMMessage = MemberName(PACKAGE_IO, "writeKMMessage")
val readKMMessage = MemberName(PACKAGE_IO, "readKMMessage")
val computeMapSize = MemberName(PACKAGE_IO, "computeMapSize")
val computeMessageSize = MemberName(PACKAGE_IO, "computeMessageSize")

val CodedOutputStream = ClassName(PACKAGE_IO, "CodedOutputStream")
val CodedInputStream = ClassName(PACKAGE_IO, "CodedInputStream")

val writeMap = MemberName(PACKAGE_IO, "writeMap")
val readMapEntry = MemberName(PACKAGE_IO, "readMapEntry")

val iosUnaryCallImplementation = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "unaryCallImplementation")
val iosServerSideStreamingCallImplementation = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "serverSideStreamingCallImplementation")

val byteArrayListEquals = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.util", "byteArrayListsEqual")

private val JSPB = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "JSPB")
val JSPB_BINARY_WRITER = JSPB.nestedClass("BinaryWriter")
val JSPB_BINARY_READER = JSPB.nestedClass("BinaryReader")