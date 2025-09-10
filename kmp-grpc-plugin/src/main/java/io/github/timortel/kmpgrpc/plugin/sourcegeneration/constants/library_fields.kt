package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member

val kmMetadata = ClassName(PACKAGE_METADATA, "Metadata")
val kmStub = ClassName(PACKAGE_STUB, "Stub")
val kmChannel = ClassName(PACKAGE_BASE, "Channel")

val nativeJsStub = ClassName(PACKAGE_STUB, "NativeJsStub")
val nativeJsCallOptions = ClassName(PACKAGE_BASE, "CallOptions")

val MessageDeserializer = ClassName(PACKAGE_MESSAGE, "MessageDeserializer")
val MessageCompanion = ClassName(PACKAGE_MESSAGE, "MessageCompanion")

val kmMessage = ClassName(PACKAGE_MESSAGE, "Message")
val kmMessageWithExtensions = ClassName(PACKAGE_MESSAGE, "MessageWithExtensions")
val kmEnum = ClassName(PACKAGE_MESSAGE, "Enum")

private val kmExtension = ClassName(PACKAGE_MESSAGE_EXTENSIONS, "Extension")
val kmExtensionScalar = kmExtension.nestedClass("ScalarValueExtension")
val kmExtensionRepeated = kmExtension.nestedClass("RepeatedValueExtension")
val kmExtensionRepeatedPackable = kmExtension.nestedClass("PackableRepeatedValueExtension")
val kmExtensionRepeatedNonPackable = kmExtension.nestedClass("NonPackableRepeatedValueExtension")

val kmMessageExtensions = ClassName(PACKAGE_MESSAGE_EXTENSIONS, "MessageExtensions")
val kmExtensionRegistry = ClassName(PACKAGE_MESSAGE_EXTENSIONS, "ExtensionRegistry")
val kmExtensionBuilder = ClassName(PACKAGE_MESSAGE_EXTENSIONS, "MessageExtensionsBuilder")

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

private val fieldType = ClassName(PACKAGE_MESSAGE, "FieldType")
val fieldTypeFloat = fieldType.nestedClass("Float")
val fieldTypeDouble = fieldType.nestedClass("Double")
val fieldTypeInt32 = fieldType.nestedClass("Int32")
val fieldTypeInt64 = fieldType.nestedClass("Int64")
val fieldTypeUInt32 = fieldType.nestedClass("UInt32")
val fieldTypeUInt64 = fieldType.nestedClass("UInt64")
val fieldTypeFixed32 = fieldType.nestedClass("Fixed32")
val fieldTypeFixed64 = fieldType.nestedClass("Fixed64")
val fieldTypeSFixed32 = fieldType.nestedClass("SFixed32")
val fieldTypeSFixed64 = fieldType.nestedClass("SFixed64")
val fieldTypeSInt32 = fieldType.nestedClass("SInt32")
val fieldTypeSInt64 = fieldType.nestedClass("SInt64")
val fieldTypeBool = fieldType.nestedClass("Bool")
val fieldTypeString = fieldType.nestedClass("String")
val fieldTypeMessage = fieldType.nestedClass("Message")
val fieldTypeEnum = fieldType.nestedClass("Enum")
val fieldTypeBytes = fieldType.nestedClass("Bytes")

// util
val mergeUnknownFieldOrExtension = MemberName(PACKAGE_MESSAGE, "mergeUnknownFieldOrExtension")
