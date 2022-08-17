package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute

object IOSToCommonMapMapper : MapMapper() {

    override fun handleCreatedMap(
        builderVariable: String,
        mapToPutVariable: CodeBlock,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        mapType: MapType
    ): CodeBlock {
        TODO("Not yet implemented")
    }

    override fun mapVariable(
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        types: Types
    ): CodeBlock {
        TODO("Not yet implemented")
    }
}