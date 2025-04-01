package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

val kmMetadata = ClassName(PACKAGE_BASE, "KMMetadata")
val kmStub = ClassName(PACKAGE_STUB, "KMStub")
val kmChannel = ClassName(PACKAGE_BASE, "KMChannel")
val kmAndroidJVMStub = ClassName(PACKAGE_STUB, "AndroidJvmKMStub")
val kmTimeUnit = ClassName(PACKAGE_UTIL, "TimeUnit")

val JSImpl = ClassName(PACKAGE_MESSAGE, "JSImpl")
val MessageDeserializer = ClassName(PACKAGE_MESSAGE, "MessageDeserializer")
val MessageCompanion = ClassName(PACKAGE_MESSAGE, "KMMessageCompanion")

val kmMessage = ClassName(PACKAGE_MESSAGE, "KMMessage")
val kmEnum = ClassName(PACKAGE_MESSAGE, "KmEnum")

val writeKMMessage = MemberName(PACKAGE_IO, "writeKMMessage")
val readKMMessage = MemberName(PACKAGE_IO, "readKMMessage")
val computeMapSize = MemberName(PACKAGE_IO, "computeMapSize")
val computeMessageSize = MemberName(PACKAGE_IO, "computeMessageSize")

val CodedOutputStream = ClassName(PACKAGE_IO, "CodedOutputStream")
val CodedInputStream = ClassName(PACKAGE_IO, "CodedInputStream")

val writeMap = MemberName(PACKAGE_IO, "writeMap")
val readMapEntry = MemberName(PACKAGE_IO, "readMapEntry")

val iosUnaryCallImplementation = MemberName(PACKAGE_RPC, "unaryCallImplementation")
val iosServerSideStreamingCallImplementation = MemberName(PACKAGE_RPC, "serverSideStreamingCallImplementation")

val byteArrayListEquals = MemberName(PACKAGE_UTIL, "byteArrayListsEqual")

private val JSPB = ClassName(PACKAGE_BASE, "JSPB")
val JSPB_BINARY_WRITER = JSPB.nestedClass("BinaryWriter")
val JSPB_BINARY_READER = JSPB.nestedClass("BinaryReader")