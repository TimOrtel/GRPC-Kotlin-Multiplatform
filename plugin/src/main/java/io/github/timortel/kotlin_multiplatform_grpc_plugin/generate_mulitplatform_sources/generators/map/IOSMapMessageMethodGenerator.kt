package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute

object IOSMapMessageMethodGenerator : MapMessageMethodGenerator(true) {
    override val modifiers: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyMapProperty(
        builder: PropertySpec.Builder,
        protoMessage: ProtoMessage,
        messageAttribute: ProtoMessageAttribute
    ) {
        TODO("Not yet implemented")
    }
}