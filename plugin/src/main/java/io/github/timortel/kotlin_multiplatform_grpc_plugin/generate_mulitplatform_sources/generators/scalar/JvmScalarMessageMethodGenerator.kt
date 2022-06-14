package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JvmScalarMessageMethodGenerator : ScalarMessageMethodGenerator(true) {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getTypeForAttribute(protoMessageAttribute: ProtoMessageAttribute): TypeName = protoMessageAttribute.commonType

    override fun modifyGetter(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        val implName = Const.Message.Constructor.JVM.PARAM_IMPL

        if (attr.types.isEnum) {
            builder.addCode("return %T.%N(%N.${attr.name}Value)", attr.commonType, Const.Enum.getEnumForNumFunctionName, implName)
        } else {
            val getter = CodeBlock.of("%N.%N", implName, attr.name)

            builder.addCode("return ")
            if (attr.types.doDiffer) {
                if (!attr.types.hasDefaultValue) {
                    builder.addCode("if (")
                    builder.addCode(getter)
                    builder.addCode(" == null) null else ")
                }

                builder.addCode("%M(", Const.Message.CommonFunction.JVM.commonFunction(attr))
                builder.addCode(getter)
                builder.addCode(")")
            } else {
                builder.addCode(getter)
            }
        }
    }

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.addStatement("return %N.has${attr.capitalizedName}()", Const.Message.Constructor.JVM.PARAM_IMPL)
    }
}