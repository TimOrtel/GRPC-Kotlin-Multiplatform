package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

const val COCOAPODS_PROTOBUF_PACKAGE = "cocoapods.Protobuf"

val GPBCodedInputStream = ClassName(COCOAPODS_PROTOBUF_PACKAGE, "GPBCodedInputStream")
val NSData = ClassName("platform.Foundation", "NSData")
val NSMutableData = ClassName("platform.Foundation", "NSMutableData")