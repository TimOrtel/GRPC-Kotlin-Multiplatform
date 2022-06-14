package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf

object CommonOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator() {

    override val attrs: List<KModifier> = emptyList()

    override fun modifyGetCaseProperty(
        builder: PropertySpec.Builder,
        enumClassName: ClassName,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) = Unit
}