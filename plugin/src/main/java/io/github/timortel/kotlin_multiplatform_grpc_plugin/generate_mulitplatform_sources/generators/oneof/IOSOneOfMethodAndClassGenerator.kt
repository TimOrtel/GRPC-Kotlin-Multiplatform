package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf

object IOSOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator() {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyGetCaseProperty(
        builder: PropertySpec.Builder,
        enumClassName: ClassName,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) {
        TODO("Not yet implemented")
    }
}