package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageField

object CommonScalarMessageMethodGenerator : ScalarMessageMethodGenerator(false) {

    override val attrs: List<KModifier> = emptyList()

    override fun getTypeForAttribute(protoMessageField: ProtoMessageField): TypeName = protoMessageField.commonType

    override fun modifyProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageField) = Unit

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageField) = Unit
}