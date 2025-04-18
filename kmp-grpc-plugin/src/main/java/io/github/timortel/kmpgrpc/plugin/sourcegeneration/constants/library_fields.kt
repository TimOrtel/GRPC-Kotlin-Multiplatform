package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

val kmMetadata = ClassName(PACKAGE_BASE, "Metadata")
val kmStub = ClassName(PACKAGE_STUB, "KMStub")
val kmChannel = ClassName(PACKAGE_BASE, "Channel")
val kmTimeUnit = ClassName(PACKAGE_UTIL, "TimeUnit")

val MessageDeserializer = ClassName(PACKAGE_MESSAGE, "MessageDeserializer")
val MessageCompanion = ClassName(PACKAGE_MESSAGE, "MessageCompanion")

val kmMessage = ClassName(PACKAGE_MESSAGE, "Message")
val kmEnum = ClassName(PACKAGE_MESSAGE, "Enum")

val unknownField = ClassName(PACKAGE_MESSAGE, "UnknownField")

val computeUnknownFieldsRequiredSize = MemberName(PACKAGE_IO, "computeUnknownFieldsRequiredSize")

val computeMapSize = MemberName(PACKAGE_IO, "computeMapSize")
val computeMessageSize = MemberName(PACKAGE_IO, "computeMessageSize")

val CodedOutputStream = ClassName(PACKAGE_IO, "CodedOutputStream")
val CodedInputStream = ClassName(PACKAGE_IO, "CodedInputStream")

val iosUnaryCallImplementation = MemberName(PACKAGE_RPC, "unaryCallImplementation")
val iosServerSideStreamingCallImplementation = MemberName(PACKAGE_RPC, "serverSideStreamingCallImplementation")

val byteArrayListEquals = MemberName(PACKAGE_UTIL, "byteArrayListsEqual")
