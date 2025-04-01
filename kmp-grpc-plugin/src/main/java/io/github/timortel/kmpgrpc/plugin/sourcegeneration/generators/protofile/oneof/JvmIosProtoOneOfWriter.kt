package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.RequiredSizePropertyExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf

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
                        is ChildClassType.Normal -> RequiredSizePropertyExtension.getCodeForRequiredSizeForScalarAttributeC(
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
