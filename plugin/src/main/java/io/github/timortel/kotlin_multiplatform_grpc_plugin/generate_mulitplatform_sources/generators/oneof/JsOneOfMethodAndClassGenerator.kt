package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JsOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyOneOfProperty(
        builder: PropertySpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) {
        builder.getter(
            FunSpec
                .getterBuilder()
                .apply {
                    addCode("return when(jsImpl.%N()) {\n", Const.Message.OneOf.JS.getCaseFunctionName(oneOf))
                    oneOf.attributes.forEach { attr ->
                        addCode(
                            "%L -> %T(%N.%N())\n",
                            attr.protoId,
                            Const.Message.OneOf.childClassName(message, oneOf, attr),
                            Const.Message.Constructor.JS.PARAM_IMPL,
                            Const.Message.Attribute.Scalar.JS.getFunction(message, attr)
                        )
                    }
                    addCode("0 -> %T\n", Const.Message.OneOf.notSetClassName(message, oneOf))
                    addCode("else -> %T\n", Const.Message.OneOf.unknownClassName(message, oneOf))
                    addCode("}")
                }
                .build()
        )
    }
}