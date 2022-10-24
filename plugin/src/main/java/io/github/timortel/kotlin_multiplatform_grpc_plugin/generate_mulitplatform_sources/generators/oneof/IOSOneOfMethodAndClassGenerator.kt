package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.IOSProtoFileWriter

object IOSOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator(true) {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyOneOfProperty(builder: PropertySpec.Builder, message: ProtoMessage, oneOf: ProtoOneOf) {
        builder.initializer(Const.Message.OneOf.propertyName(message, oneOf))
    }

    override fun modifyParentClass(builder: TypeSpec.Builder, message: ProtoMessage, oneOf: ProtoOneOf) {
        builder.addProperty(Const.Message.OneOf.IOS.REQUIRED_SIZE_PROPERTY_NAME, U_LONG, KModifier.ABSTRACT)
        addSerializeFunction(builder, listOf(KModifier.ABSTRACT)) {

        }
    }

    override fun modifyChildClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf,
        childClassType: ChildClassType
    ) {
        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.OneOf.IOS.REQUIRED_SIZE_PROPERTY_NAME,
                    U_LONG,
                    KModifier.OVERRIDE
                )
                .initializer(
                    when (childClassType) {
                        is ChildClassType.Normal -> IOSProtoFileWriter.getCodeForRequiredSizeForScalarAttributeC(
                            childClassType.attr
                        )

                        ChildClassType.NotSet -> CodeBlock.of("0u")
                        /*
                        If KM-GRPC wants to conform to proto 3.5, unknown fields must be retained.
                         */
                        ChildClassType.Unknown -> CodeBlock.of("0u")
                    }
                )
                .build()
        )

        addSerializeFunction(builder, listOf(KModifier.OVERRIDE)) {
            when (childClassType) {
                is ChildClassType.Normal -> addCode(
                    IOSProtoFileWriter.getWriteScalarFieldCode(
                        message,
                        childClassType.attr,
                        Const.Message.OneOf.IOS.SERIALIZE_FUNCTION_STREAM_PARAM_NAME,
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
                .builder(Const.Message.OneOf.IOS.SERIALIZE_FUNCTION_NAME)
                .addModifiers(modifiers)
                .addParameter(Const.Message.OneOf.IOS.SERIALIZE_FUNCTION_STREAM_PARAM_NAME, CodedOutputStream)
                .apply(modify)
                .build()
        )
    }
}