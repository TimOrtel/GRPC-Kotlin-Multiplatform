package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JsOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator() {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyGetCaseProperty(
        builder: PropertySpec.Builder,
        enumClassName: ClassName,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) {
        val enumName = Const.Message.OneOf.CaseEnum.oneOfCaseClassName(oneOf)

        builder.getter(
            FunSpec
                .getterBuilder()
                .apply {
                    addCode("return when(jsImpl.%N()) {\n", Const.Message.OneOf.JS.getCaseFunctionName(oneOf))
                    oneOf.attributes.forEach { attr ->
                        addCode(
                            "%L -> %T.%N.%N\n",
                            attr.protoId,
                            message.commonType,
                            enumName,
                            Const.Message.OneOf.CaseEnum.EnumField.name(attr)
                        )
                    }
                    addCode("0 -> %T.%N.%N\n", message.commonType, enumName, Const.Message.OneOf.CaseEnum.EnumNotSet.name(oneOf))
                    addCode("else -> null\n")
                    addCode("}")
                }
                .build()
        )
    }
}