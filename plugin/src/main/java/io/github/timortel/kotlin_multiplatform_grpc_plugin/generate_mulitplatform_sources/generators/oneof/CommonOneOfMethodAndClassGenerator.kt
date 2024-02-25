package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf

object CommonOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator(false) {

    override val attrs: List<KModifier> = emptyList()

    override fun modifyOneOfProperty(
        builder: PropertySpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) = Unit
}