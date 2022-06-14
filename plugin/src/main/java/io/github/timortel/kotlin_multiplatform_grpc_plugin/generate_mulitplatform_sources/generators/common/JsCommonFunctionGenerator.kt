package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.common

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage

class JsCommonFunctionGenerator(private val builder: FileSpec.Builder) : CommonFunctionGenerator() {

    override fun addFunction(funSpec: FunSpec) {
        builder.addFunction(funSpec)
    }

    override fun getNativePlatformType(message: ProtoMessage): TypeName = message.jsType

}