package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

abstract class MapMessageMethodGenerator {

    private companion object {
        private val mapType = Map::class.asTypeName()
    }

    protected abstract val modifiers: List<KModifier>

    fun generateFunctions(
        builder: TypeSpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageAttribute
    ) {
        val attributeType = messageAttribute.attributeType as MapType

        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.Attribute.Map.propertyName(messageAttribute),
                    mapType.parameterizedBy(attributeType.keyTypes.commonType, attributeType.valueTypes.commonType)
                )
                .addModifiers(modifiers)
                .apply {
                    modifyMapProperty(this, protoMessage, messageAttribute)
                }
                .build()
        )
    }

    protected abstract fun modifyMapProperty(builder: PropertySpec.Builder, protoMessage: ProtoMessage, messageAttribute: ProtoMessageAttribute)
}