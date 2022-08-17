package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.CommonToJsMapMapper
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.MapMapper
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute

object JsDslBuilder : SubDslBuilder(true) {

    override val mapMapper: MapMapper = CommonToJsMapMapper

    override fun initializeBuilder(builder: FunSpec.Builder, message: ProtoMessage): String {
        builder.addStatement("val jsImpl = %T()", message.jsType)
        return "jsImpl"
    }

    override fun returnPlatformType(builder: FunSpec.Builder, message: ProtoMessage, builderVariableName: String) {
        builder.addStatement("return %T(jsImpl)", message.commonType)
    }

    override fun setEnumValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) {
        val f = Const.Message.Attribute.Scalar.JS.setFunction(message, attribute).simpleName
        val v = CodeBlock.of("%N?.value ?: 0", variableName)

        builder.addCode("%N.%N(", builderVariable, f)
        builder.addCode(v)
        builder.addCode(")\n")
    }

    override fun setScalarValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) {
        val f = Const.Message.Attribute.Scalar.JS.setFunction(message, attribute).simpleName
        val v = if (attribute.types.doDiffer) {
            CodeBlock.of(
                "%N.%N",
                variableName,
                Const.Message.Constructor.JS.PARAM_IMPL
            )
        } else {
            CodeBlock.of("%N", variableName)
        }

        builder.addCode("%N.%N(", builderVariable, f)
        builder.addCode(v)
        builder.addCode(")\n")
    }

    override fun addAllValues(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) {
        builder.addCode(
            "%N.%N(",
            builderVariable,
            Const.Message.Attribute.Repeated.JS.setListFunctionName(attribute)
        )

        if (attribute.types.doDiffer) {
            builder.addCode(
                "%N.map·{ it.%N }.toTypedArray()",
                Const.DSL.Attribute.Repeated.attrName(attribute),
                Const.Message.Constructor.JS.PARAM_IMPL
            )
        } else {
            builder.addCode("%N.toTypedArray()", Const.DSL.Attribute.Repeated.attrName(attribute))
        }

        builder.addCode(")\n")
    }

    override fun addAllEnumValues(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) {
        builder.addCode(
            "%N.%N(",
            builderVariable,
            Const.Message.Attribute.Repeated.JS.setListFunctionName(attribute)
        )

        builder.addCode(
            "%N.map·{ it.%N }.toTypedArray())\n",
            Const.DSL.Attribute.Repeated.attrName(attribute),
            "value"
        )
    }
}