package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object ActualScalarMessageMethodGenerator : ScalarMessageMethodGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getTypeForAttribute(protoMessageField: ProtoMessageField): TypeName =
        protoMessageField.commonType

    override fun modifyProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageField) {
        when (attr.types.protoType) {
            ProtoType.MESSAGE -> builder.initializer("%N ?: %T()", attr.name, attr.commonType)
            else -> builder.initializer(attr.name)
        }
    }

    override fun generateProperties(
        builder: TypeSpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageField,
        type: TypeName
    ) {
        super.generateProperties(builder, protoMessage, messageAttribute, type)

        //Additionally generate a property that tells if a message is set.
        if (messageAttribute.types.protoType == ProtoType.MESSAGE) {
            builder.addProperty(
                PropertySpec
                    .builder(
                        Const.Message.Attribute.Scalar.IosJvm.isMessageSetFunctionName(protoMessage, messageAttribute),
                        BOOLEAN,
                        KModifier.PRIVATE
                    )
                    .initializer("%N != null", messageAttribute.name)
                    .build()
            )
        }
    }

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageField) {
        builder.apply {
            addStatement("return %N", Const.Message.Attribute.Scalar.IosJvm.isMessageSetFunctionName(message, attr))
        }
    }
}