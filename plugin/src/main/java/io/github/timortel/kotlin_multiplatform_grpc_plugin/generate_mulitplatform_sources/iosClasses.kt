package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.ClassName

val GPBCodedOutputStream = ClassName("cocoapods.Protobuf", "GPBCodedOutputStream")
val NSData = ClassName("platform.Foundation", "NSData")
val NSMutableData = ClassName("platform.Foundation", "NSMutableData")