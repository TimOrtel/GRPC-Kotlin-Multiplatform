package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object IosJvmDslBuilder : DslBuilder(true) {

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addCode("return %T(", message.commonType)
            message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                val propertyName = Const.Message.Attribute.propertyName(message, attr)

                addCode("\n%N = %N ?: ", propertyName, propertyName)
                addCode(attr.commonDefaultValue(mutable = false, useEmptyMessage = false))
                addCode(",")
            }
            message.oneOfs.forEach { oneOf ->
                addCode(
                    "\n%N = %N,",
                    Const.Message.OneOf.propertyName(message, oneOf),
                    Const.DSL.OneOf.propertyName(message, oneOf)
                )
            }
            addCode("\n)")
        }
    }
}