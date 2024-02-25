package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.ActualProtoFileWriter

abstract class ActualOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyOneOfProperty(builder: PropertySpec.Builder, message: ProtoMessage, oneOf: ProtoOneOf) {
        builder.initializer(Const.Message.OneOf.propertyName(message, oneOf))
    }

    override fun modifyParentClass(builder: TypeSpec.Builder, message: ProtoMessage, oneOf: ProtoOneOf) {
        addSerializeFunction(
            builder,
            listOf(KModifier.ABSTRACT)
        ) {

        }
    }

    override fun modifyChildClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf,
        childClassType: ChildClassType
    ) {
        addSerializeFunction(builder, listOf(KModifier.OVERRIDE)) {
            when (childClassType) {
                is ChildClassType.Normal -> addCode(
                    ActualProtoFileWriter.getWriteScalarFieldCode(
                        message,
                        childClassType.attr,
                        Const.Message.OneOf.IosJvm.SERIALIZE_FUNCTION_STREAM_PARAM_NAME,
                        performIsMessageSetCheck = false
                    )
                )
                ChildClassType.Unknown, ChildClassType.NotSet -> {
                }
            }
        }
    }

    private fun addSerializeFunction(
        builder: TypeSpec.Builder,
        modifiers: List<KModifier>,
        modify: FunSpec.Builder.() -> Unit
    ) {
        builder.addFunction(
            FunSpec
                .builder(Const.Message.OneOf.IosJvm.SERIALIZE_FUNCTION_NAME)
                .addModifiers(modifiers)
                .addParameter(Const.Message.OneOf.IosJvm.SERIALIZE_FUNCTION_STREAM_PARAM_NAME, CodedOutputStream)
                .apply(modify)
                .build()
        )
    }
}