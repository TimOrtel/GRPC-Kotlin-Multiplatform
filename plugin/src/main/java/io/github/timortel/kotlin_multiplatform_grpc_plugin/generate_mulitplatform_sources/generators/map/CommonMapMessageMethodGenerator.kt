package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageField

object CommonMapMessageMethodGenerator : MapMessageMethodGenerator() {

    override val modifiers: List<KModifier> = listOf(KModifier.EXPECT)

    override fun modifyMapProperty(
        builder: PropertySpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageField
    ) = Unit
}