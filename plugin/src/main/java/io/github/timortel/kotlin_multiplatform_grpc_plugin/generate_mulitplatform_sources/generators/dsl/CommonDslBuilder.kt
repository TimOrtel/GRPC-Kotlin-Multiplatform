package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.MapMapper

object CommonDslBuilder : DslBuilder(false) {

    override val mapMapper: MapMapper
        get() = error("No mapper needed for common")

    override fun initializeBuilder(builder: FunSpec.Builder, message: ProtoMessage): String = error("Common does not have an implementation")

    override fun returnPlatformType(builder: FunSpec.Builder, message: ProtoMessage, builderVariableName: String) = Unit

    override fun setEnumValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) = Unit

    override fun setScalarValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) = Unit

    override fun addAllValues(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) = Unit

    override fun addAllEnumValues(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    ) = Unit
}