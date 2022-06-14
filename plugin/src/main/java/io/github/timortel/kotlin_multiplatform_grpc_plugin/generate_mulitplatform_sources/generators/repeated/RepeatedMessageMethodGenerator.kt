package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

/**
 * generates methods for repeated fields.
 */
abstract class RepeatedMessageMethodGenerator(private val isActual: Boolean) {

    protected abstract val attrs: List<KModifier>

    fun generateFunctions(builder: TypeSpec.Builder, message: ProtoMessage, messageAttribute: ProtoMessageAttribute) {
        val type = getType(messageAttribute)

        val listProperty = PropertySpec
            .builder(
                Const.Message.Attribute.Repeated.listPropertyName(messageAttribute),
                ClassName("kotlin.collections", "List").parameterizedBy(type)
            )
            .addModifiers(attrs)
            .apply { modifyListProperty(this, message, messageAttribute) }
            .build()

        val countProperty = PropertySpec
            .builder(Const.Message.Attribute.Repeated.countPropertyName(messageAttribute), Int::class)
            .addModifiers(attrs)
            .apply { modifyCountProperty(this, message, messageAttribute) }
            .build()

        builder.addProperty(listProperty)
        builder.addProperty(countProperty)
    }

    protected abstract fun getType(messageAttribute: ProtoMessageAttribute): TypeName

    protected abstract fun modifyListProperty(
        builder: PropertySpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageAttribute
    )

    protected open fun modifyCountProperty(
        builder: PropertySpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageAttribute
    ) {
        if (isActual) {
            builder.initializer("%N.size", Const.Message.Attribute.Repeated.listPropertyName(attr))
        }
    }
}