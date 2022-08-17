package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JsScalarMessageMethodGenerator : ScalarMessageMethodGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getTypeForAttribute(protoMessageAttribute: ProtoMessageAttribute): TypeName =
        protoMessageAttribute.commonType

    override fun modifyProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.getter(FunSpec.getterBuilder().apply {
            if (attr.types.isEnum) {
                addCode(
                    "return %T.%N(jsImpl.%N())",
                    attr.commonType,
                    Const.Enum.getEnumForNumFunctionName,
                    Const.Message.Attribute.Scalar.JS.getFunction(message, attr).simpleName
                )
            } else {
                val getter =
                    CodeBlock.of(
                        "jsImpl.%N()",
                        Const.Message.Attribute.Scalar.JS.getFunction(message, attr).simpleName
                    )

                addCode("return ")
                if (attr.types.doDiffer) {
//                    if (attr.types.hasDefaultValue) {
                    addCode("%M(", Const.Message.CommonFunction.JS.commonFunction(attr))
                    addCode(getter)
                    addCode(")")
//                    } else {
//                        addCode(getter)
//                        addCode(
//                            ".let·{ if (it == null) null else %M(it) }",
//                            Const.Message.CommonFunction.JS.commonFunction(attr)
//                        )
//                    }
                } else {
                    addCode(getter)
                }
            }
        }.build())
    }

//    override fun modifySetter(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
//        val value =
//            if (attr.types.isEnum) "value.value"
//            else if (attr.types.doDiffer) {
//                if (attr.types.hasDefaultValue) {
//                    "value.jsImpl"
//                } else "value?.jsImpl ?: undefined"
//            } else {
//                if (attr.types.hasDefaultValue) {
//                    "value"
//                } else {
//                    "value ?: undefined"
//                }
//            }
//
//        builder.addStatement("jsImpl.%N($value)", Const.Message.Attribute.Scalar.JS.setFunction(message, attr).simpleName)
//    }

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.addStatement(
            "return jsImpl.%N()",
            Const.Message.Attribute.Scalar.JS.getHasFunction(message, attr).simpleName
        )
    }
}