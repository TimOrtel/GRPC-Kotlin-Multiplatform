package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JsRepeatedMessageMethodGenerator : RepeatedMessageMethodGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getType(messageAttribute: ProtoMessageAttribute): TypeName = messageAttribute.commonType

    override fun modifyListProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        val initBlock = CodeBlock.builder()

        initBlock.add(
            "jsImpl.%N()",
            Const.Message.Attribute.Repeated.JS.getListFunctionName(attr)
        )

        if (attr.types.isEnum) {
            initBlock.add(
                ".map·{ %T.%N(it) }.toList()",
                attr.types.commonType,
                Const.Enum.getEnumForNumFunctionName
            )
        } else if (attr.types.doDiffer) {
            initBlock.add(".map·{ %M(it) }.toList()", Const.Message.CommonFunction.JS.commonFunction(attr))
        } else {
            initBlock.add(".toList()")
        }

        builder.initializer(initBlock.build())
    }
}