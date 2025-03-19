package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageField

/**
 * Generates methods for scalar proto fields. Scalar means the simple, non-repeated message field.
 */
abstract class ScalarMessageMethodGenerator(private val isActual: Boolean) {

    protected abstract val attrs: List<KModifier>

    fun generateProperties(
        builder: TypeSpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageField
    ) {
        val type = getTypeForAttribute(messageAttribute)

        //No property needed for one of attributes
        if (!messageAttribute.isOneOfAttribute) {
            generateProperties(builder, protoMessage, messageAttribute, type)
        }
    }

    open fun generateProperties(
        builder: TypeSpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageField,
        type: TypeName
    ) {
        builder.addProperty(
            PropertySpec
                .builder(messageAttribute.name, type)
                .addModifiers(attrs)
                .apply {
                    modifyProperty(this, protoMessage, messageAttribute)
                }
                .build()
        )

        if (messageAttribute.types.isNullable) {
            //add an extra has function
            builder.addFunction(
                FunSpec
                    .builder("has${messageAttribute.capitalizedName}")
                    .addModifiers(attrs)
                    .returns(Boolean::class)
                    .apply { modifyHasFunction(this, protoMessage, messageAttribute) }
                    .build()
            )
        }
    }

    protected abstract fun getTypeForAttribute(protoMessageField: ProtoMessageField): TypeName

    protected abstract fun modifyProperty(
        builder: PropertySpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageField
    )

    protected abstract fun modifyHasFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageField
    )
}