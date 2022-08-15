package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JvmScalarMessageMethodGenerator : ScalarMessageMethodGenerator(true) {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getTypeForAttribute(protoMessageAttribute: ProtoMessageAttribute): TypeName = protoMessageAttribute.commonType

    override fun modifyProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.getter(
            FunSpec
                .getterBuilder()
                .apply {
                    val implName = Const.Message.Constructor.JVM.PARAM_IMPL

                    if (attr.types.isEnum) {
                        addCode("return %T.%N(%N.${attr.name}Value)", attr.commonType, Const.Enum.getEnumForNumFunctionName, implName)
                    } else {
                        val getter = CodeBlock.of("%N.%N", implName, attr.name)

                        addCode("return ")
                        if (attr.types.doDiffer) {
                            if (!attr.types.hasDefaultValue) {
                                addCode("if (")
                                addCode(getter)
                                addCode(" == null) null else ")
                            }

                            addCode("%M(", Const.Message.CommonFunction.JVM.commonFunction(attr))
                            addCode(getter)
                            addCode(")")
                        } else {
                            addCode(getter)
                        }
                    }
                }
                .build()
        )
    }

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.addStatement("return %N.has${attr.capitalizedName}()", Const.Message.Constructor.JVM.PARAM_IMPL)
    }
}