package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.oneof

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.message.ActualProtoMessageWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.message.IosJvmProtoMessageWriteBase
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.ProtoOneOf

object JvmIosProtoOneOfWriter : ActualProtoOneOfWriter() {

    override fun modifyParentClass(builder: TypeSpec.Builder, oneOf: ProtoOneOf) {
        super.modifyParentClass(builder, oneOf)

        builder.addProperty(Const.Message.OneOf.REQUIRED_SIZE_PROPERTY_NAME, INT, KModifier.ABSTRACT)
    }

    override fun modifyChildClass(builder: TypeSpec.Builder, oneOf: ProtoOneOf, childClassType: ChildClassType) {
        super.modifyChildClass(builder, oneOf, childClassType)

        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.OneOf.REQUIRED_SIZE_PROPERTY_NAME,
                    INT,
                    KModifier.OVERRIDE
                )
                .initializer(
                    when (childClassType) {
                        is ChildClassType.Normal -> IosJvmProtoMessageWriteBase.getCodeForRequiredSizeForScalarAttributeC(
                            childClassType.field
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
}
