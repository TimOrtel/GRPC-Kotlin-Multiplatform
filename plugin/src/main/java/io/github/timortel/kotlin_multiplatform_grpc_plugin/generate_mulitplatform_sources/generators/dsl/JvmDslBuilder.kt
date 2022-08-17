package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.CommonToJvmMapMapper
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.MapMapper
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute

object JvmDslBuilder : SubDslBuilder(true) {

    override val mapMapper: MapMapper = CommonToJvmMapMapper

    override fun initializeBuilder(builder: FunSpec.Builder, message: ProtoMessage): String {
        builder.addStatement("val builder = %T.newBuilder()", message.jvmType)
        return "builder"
    }

    override fun returnPlatformType(builder: FunSpec.Builder, message: ProtoMessage, builderVariableName: String) {
        builder.apply {
            addStatement("val result = %N.build()", builderVariableName)
            addStatement("return %T(result)", message.commonType)
        }
    }

    override fun setEnumValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) {
        val f = Const.Message.Attribute.Scalar.JVM.setEnumValueFunction(message, attribute).simpleName
        val v = "$variableName?.value ?: 0"

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
        val f = Const.Message.Attribute.Scalar.JVM.setFunction(message, attribute).simpleName
        val v = if (attribute.types.doDiffer) {
            CodeBlock.of(
                "%N.%N",
                variableName,
                Const.Message.Constructor.JVM.PARAM_IMPL
            )
        } else {
            CodeBlock.of(variableName)
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
            Const.Message.Attribute.Repeated.JVM.addAllFunction(message, attribute).simpleName
        )

        if (attribute.types.doDiffer) {
            builder.addCode(
                "%N.map·{ it.%N }",
                Const.DSL.Attribute.Repeated.attrName(attribute),
                Const.Message.Constructor.JVM.PARAM_IMPL
            )
        } else {
            builder.addCode("%N", Const.DSL.Attribute.Repeated.attrName(attribute))
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
            Const.Message.Attribute.Repeated.JVM.addAllValuesFunction(message, attribute).simpleName
        )

        builder.addCode(
            "%N.map·{ it.%N })\n",
            Const.DSL.Attribute.Repeated.attrName(attribute),
            "value"
        )
    }
}