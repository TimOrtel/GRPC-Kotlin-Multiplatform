package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object JvmProtoMessageWriter : IosJvmProtoMessageWriteBase() {

    private const val PACKAGE_PROTOBUF = "com.google.protobuf"

    private val MESSAGE_LITE = ClassName(PACKAGE_PROTOBUF, "MessageLite")
    val PROTO_CODED_INPUT_STREAM = ClassName(PACKAGE_PROTOBUF, "CodedInputStream")

    override val additionalSuperinterfaces: List<TypeName> = listOf(MESSAGE_LITE)

    override val target: SourceTarget = SourceTarget.Jvm

    override fun applyToClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage
    ) {
        super.applyToClass(builder, message)

        builder.apply {
            //Default implementation for MessageLite interface
            addFunction(
                FunSpec
                    .builder("getDefaultInstanceForType")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(MESSAGE_LITE)
                    .addStatement("return %T()", message.className)
                    .build()
            )


            addFunction(
                FunSpec
                    .builder("getParserForType")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(
                        ClassName(PACKAGE_PROTOBUF, "Parser")
                            .parameterizedBy(WildcardTypeName.producerOf(MESSAGE_LITE))
                    )
                    .addCode(CodeBlock.builder().apply {
                        val parserObject = TypeSpec.anonymousClassBuilder()
                            .superclass(
                                ClassName(PACKAGE_PROTOBUF, "AbstractParser")
                                    .parameterizedBy(message.className)
                            )
                            .addFunction(
                                FunSpec
                                    .builder("parsePartialFrom")
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addParameter("input", PROTO_CODED_INPUT_STREAM)
                                    .addParameter(
                                        "extensionRegistry",
                                        ClassName(PACKAGE_PROTOBUF, "ExtensionRegistryLite").copy(nullable = true)
                                    )
                                    .returns(message.className)
                                    .addStatement("return %N(input)", Const.Message.Companion.WrapperDeserializationFunction.NAME)
                                    .build()
                            )
                            .build()

                        addStatement(
                            "return %L",
                            parserObject
                        )
                    }
                        .build()
                    )
                    .build()
            )
        }
    }
}
