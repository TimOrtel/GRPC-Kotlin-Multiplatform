package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.ActualProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.IosJvmProtoFileWriteBase
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.ProtoFileWriter

object IosJvmOneOfMethodAndClassGenerator : ActualOneOfMethodAndClassGenerator() {

    override fun modifyParentClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) {
        super.modifyParentClass(builder, message, oneOf)

        builder.addProperty(Const.Message.OneOf.IosJvm.REQUIRED_SIZE_PROPERTY_NAME, INT, KModifier.ABSTRACT)
    }

    override fun modifyChildClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf,
        childClassType: ChildClassType
    ) {
        super.modifyChildClass(builder, message, oneOf, childClassType)

        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.OneOf.IosJvm.REQUIRED_SIZE_PROPERTY_NAME,
                    INT,
                    KModifier.OVERRIDE
                )
                .initializer(
                    when (childClassType) {
                        is ChildClassType.Normal -> IosJvmProtoFileWriteBase.getCodeForRequiredSizeForScalarAttributeC(
                            childClassType.attr
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