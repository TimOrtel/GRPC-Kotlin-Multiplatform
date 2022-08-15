package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute

object IOSScalarMessageMethodGenerator : ScalarMessageMethodGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getTypeForAttribute(protoMessageAttribute: ProtoMessageAttribute): TypeName = protoMessageAttribute.commonType

    override fun modifyProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        when(attr.types.protoType) {
            ProtoType.MESSAGE -> builder.initializer("%N ?: %T()", attr.name, attr.commonType)
            else -> builder.initializer(attr.name)
        }
    }

    override fun modifyHasFunction(builder: FunSpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        builder.apply {
            addStatement("return %N != null", attr.name)
        }
    }
}