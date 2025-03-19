package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageField

object CommonRepeatedMessageMethodGenerator : RepeatedMessageMethodGenerator(false) {

    override val attrs: List<KModifier> = emptyList()

    override fun getType(messageAttribute: ProtoMessageField): TypeName = messageAttribute.commonType

    override fun modifyListProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageField) = Unit

}