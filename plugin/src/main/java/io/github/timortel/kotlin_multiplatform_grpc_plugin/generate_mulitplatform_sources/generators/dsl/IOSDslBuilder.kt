package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.ios.IOSDefaultAttributeValue

object IOSDslBuilder : DslBuilder(true) {

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addCode("return %T(", message.iosType)
            message.attributes.forEach { attr ->
                addCode("\n%N=%N ?: ", attr.name, Const.DSL.Attribute.Scalar.attrName(attr))
                addCode(IOSDefaultAttributeValue.getDefaultValueForAttr(attr))
                addCode(",")
            }
            addCode("\n)")
        }
    }
}