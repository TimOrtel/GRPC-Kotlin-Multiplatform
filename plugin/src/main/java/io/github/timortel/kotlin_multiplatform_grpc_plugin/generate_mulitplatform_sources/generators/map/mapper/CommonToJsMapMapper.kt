package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.objPropertyName

object CommonToJsMapMapper : MapMapper() {

    override fun handleCreatedMap(
        builderVariable: String,
        mapToPutVariable: CodeBlock,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        mapType: MapType
    ): CodeBlock {
        val internalMapVariableName = attribute.name

        return CodeBlock
            .builder()
            .addStatement(
                "val %N = %N.%N(false)",
                internalMapVariableName,
                builderVariable,
                Const.Message.Attribute.Map.JS.getMapFunctionName(attribute)
            )
            .add(mapToPutVariable)
            .add(".forEach·{ (k, v) -> %N.set(k, v) }\n", internalMapVariableName)
            .build()
    }

    override fun mapVariable(
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        types: Types
    ): CodeBlock {
        return if (types.isEnum) {
            CodeBlock.of("%N.value", variableName)
        } else CodeBlock.of("%N.%N.%N", variableName, Const.Message.Constructor.JS.PARAM_IMPL, objPropertyName)
    }
}