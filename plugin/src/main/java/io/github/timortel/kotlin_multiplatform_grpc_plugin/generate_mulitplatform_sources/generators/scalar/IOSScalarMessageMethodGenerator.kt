package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object IOSScalarMessageMethodGenerator : ScalarMessageMethodGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getTypeForAttribute(protoMessageAttribute: ProtoMessageAttribute): TypeName =
        protoMessageAttribute.commonType

    override fun modifyProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        when (attr.types.protoType) {
            ProtoType.MESSAGE -> builder.initializer("%N ?: %T()", attr.name, attr.commonType)
            else -> builder.initializer(attr.name)
        }
    }

    override fun generateProperties(
        builder: TypeSpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageAttribute,
        type: TypeName
    ) {
        super.generateProperties(builder, protoMessage, messageAttribute, type)

        //Additionally generate a property that tells if a message is set.
        if (messageAttribute.types.protoType == ProtoType.MESSAGE) {
            builder.addProperty(
                PropertySpec
                    .builder(
                        Const.Message.Attribute.Scalar.IOS.isMessageSetFunctionName(protoMessage, messageAttribute),
                        BOOLEAN,
                        KModifier.PRIVATE
                    )
                    .initializer("%N != null", messageAttribute.name)
                    .build()
            )
        }
    }

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.apply {
            addStatement("return %N", Const.Message.Attribute.Scalar.IOS.isMessageSetFunctionName(message, attr))
        }
    }
}