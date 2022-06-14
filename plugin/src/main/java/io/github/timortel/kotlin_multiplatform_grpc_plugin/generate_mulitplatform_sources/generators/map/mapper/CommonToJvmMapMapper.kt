package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object CommonToJvmMapMapper : MapMapper() {

    override fun handleCreatedMap(
        builderVariable: String,
        mapToPutVariable: CodeBlock,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        mapType: MapType
    ): CodeBlock {
        return CodeBlock
            .builder()
            .add(
                "%N.%N(",
                builderVariable,
                Const.Message.Attribute.Map.JVM.putAllFunctionName(attribute)
            )
            .add(mapToPutVariable)
            .add(")")
            .build()
    }

    override fun mapVariable(
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        types: Types
    ): CodeBlock {
        return if (types.isEnum) {
            CodeBlock.of("%T.forNumber(%N.value)", types.jvmType, variableName)
        } else {
            CodeBlock.of("%N.%N", variableName, Const.Message.Constructor.JVM.PARAM_IMPL)
        }
    }
}