package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member

val kmMetadata = ClassName(PACKAGE_BASE, "Metadata")
val kmStub = ClassName(PACKAGE_STUB, "Stub")
val kmChannel = ClassName(PACKAGE_BASE, "Channel")

val nativeJsStub = ClassName(PACKAGE_STUB, "NativeJsStub")
val nativeJsCallOptions = ClassName(PACKAGE_BASE, "CallOptions")

val MessageDeserializer = ClassName(PACKAGE_MESSAGE, "MessageDeserializer")
val MessageCompanion = ClassName(PACKAGE_MESSAGE, "MessageCompanion")

val kmMessage = ClassName(PACKAGE_MESSAGE, "Message")
val kmEnum = ClassName(PACKAGE_MESSAGE, "Enum")

val unknownField = ClassName(PACKAGE_MESSAGE, "UnknownField")

val CodedOutputStream = ClassName(PACKAGE_IO, "CodedOutputStream")
val CodedInputStream = ClassName(PACKAGE_IO, "CodedInputStream")

val byteArrayListEquals = MemberName(PACKAGE_UTIL, "byteArrayListsEqual")

val dataSize = ClassName(PACKAGE_IO, "DataSize")
val computeUnknownFieldsRequiredSize = dataSize.member("computeUnknownFieldsRequiredSize")
val computeMapSize = dataSize.member("computeMapSize")
val computeMessageSize = dataSize.member("computeMessageSize")
val computeMessageSizeNoTag = dataSize.member("computeMessageSizeNoTag")
val computeEnumSize = dataSize.member("computeEnumSize")
val computeEnumSizeNoTag = dataSize.member("computeEnumSizeNoTag")
val computeTagSize = dataSize.member("computeTagSize")
val computeInt32SizeNoTag = dataSize.member("computeInt32SizeNoTag")
val WireFormatMakeTag = MemberName(PACKAGE_IO, "wireFormatMakeTag")
val WireFormatForType = MemberName(PACKAGE_IO, "wireFormatForType")
val DataType = ClassName(PACKAGE_MESSAGE, "DataType")
