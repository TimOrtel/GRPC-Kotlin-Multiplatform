package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.CodedOutputStream
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.RequiredSizePropertyExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.SerializationFunctionExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf

abstract class ActualProtoOneOfWriter : ProtoOneOfWriter(true) {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyOneOfProperty(builder: PropertySpec.Builder, oneOf: ProtoOneOf) {
        builder.initializer(oneOf.name)
    }

    override fun modifyParentClass(builder: TypeSpec.Builder, oneOf: ProtoOneOf) {
        addSerializeFunction(builder, listOf(KModifier.ABSTRACT)) {}

        builder.addProperty(Const.Message.OneOf.REQUIRED_SIZE_PROPERTY_NAME, INT, KModifier.ABSTRACT)
    }

    override fun modifyChildClass(builder: TypeSpec.Builder, oneOf: ProtoOneOf, childClassType: ChildClassType) {
        addSerializeFunction(builder, listOf(KModifier.OVERRIDE)) {
            when (childClassType) {
                is ChildClassType.Normal -> addCode(
                    SerializationFunctionExtension.getWriteScalarFieldCode(
                        childClassType.field,
                        Const.Message.OneOf.SERIALIZE_FUNCTION_STREAM_PARAM_NAME,
                        performIsFieldSetCheck = false
                    )
                )

                ChildClassType.Unknown, ChildClassType.NotSet -> {
                }
            }
        }

        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.OneOf.REQUIRED_SIZE_PROPERTY_NAME,
                    INT,
                    KModifier.OVERRIDE
                )
                .initializer(
                    when (childClassType) {
                        is ChildClassType.Normal -> RequiredSizePropertyExtension.getCodeForRequiredSizeForScalarAttributeC(
                            childClassType.field,
                            isOptional = false
                        )

                        ChildClassType.NotSet -> CodeBlock.of("0")
                        /*
                        If KM-GRPC wants to conform to proto 3.5, unknown fields must be retained.
                         */
                        ChildClassType.Unknown -> CodeBlock.of("0")
                    }
                )
                .build()
        )
    }

    private fun addSerializeFunction(
        builder: TypeSpec.Builder,
        modifiers: List<KModifier>,
        modify: FunSpec.Builder.() -> Unit
    ) {
        builder.addFunction(
            FunSpec
                .builder(Const.Message.OneOf.SERIALIZE_FUNCTION_NAME)
                .addModifiers(modifiers)
                .addParameter(Const.Message.OneOf.SERIALIZE_FUNCTION_STREAM_PARAM_NAME, CodedOutputStream)
                .apply(modify)
                .build()
        )
    }
}