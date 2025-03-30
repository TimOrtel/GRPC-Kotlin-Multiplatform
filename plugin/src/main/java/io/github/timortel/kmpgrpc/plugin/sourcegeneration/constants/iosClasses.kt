package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName

const val COCOAPODS_PROTOBUF_PACKAGE = "cocoapods.Protobuf"

val GPBCodedInputStream = ClassName(COCOAPODS_PROTOBUF_PACKAGE, "GPBCodedInputStream")
val NSData = ClassName("platform.Foundation", "NSData")
val NSMutableData = ClassName("platform.Foundation", "NSMutableData")