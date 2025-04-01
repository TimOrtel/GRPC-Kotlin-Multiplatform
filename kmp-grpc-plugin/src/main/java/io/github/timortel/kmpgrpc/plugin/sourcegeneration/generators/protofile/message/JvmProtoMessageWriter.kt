package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.CodedInputStream
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.CodedOutputStream
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object JvmProtoMessageWriter : IosJvmProtoMessageWriteBase() {

    private const val PACKAGE_PROTOBUF = "com.google.protobuf"

    private val MESSAGE_LITE = ClassName(PACKAGE_PROTOBUF, "MessageLite")
    private val PROTO_CODED_OUTPUT_STREAM = ClassName(PACKAGE_PROTOBUF, "CodedOutputStream")
    val PROTO_CODED_INPUT_STREAM = ClassName(PACKAGE_PROTOBUF, "CodedInputStream")

    private val BYTE_STRING = ClassName(PACKAGE_PROTOBUF, "ByteString")

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
                    .builder("isInitialized")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(BOOLEAN)
                    .addStatement("return true")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("writeTo")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("output", PROTO_CODED_OUTPUT_STREAM)
                    .addStatement("serialize(%T(output))", CodedOutputStream)
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("writeTo")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("output", ClassName("java.io", "OutputStream"))
                    .addStatement("val bufferSize = requiredSize")
                    .addStatement("val codedOutput = %T.newInstance(output, bufferSize)", PROTO_CODED_OUTPUT_STREAM)
                    .addStatement("writeTo(codedOutput)")
                    .addStatement("codedOutput.flush()")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("getSerializedSize")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(INT)
                    .addStatement("return requiredSize")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("toByteString")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(BYTE_STRING)
                    .addStatement("return %T.copyFrom(toByteArray())", BYTE_STRING)
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("toByteArray")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(BYTE_ARRAY)
                    .addStatement("return serialize()")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("writeDelimitedTo")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("output", ClassName("java.io", "OutputStream"))
                    .addStatement("val stream = %T.newInstance(output)", PROTO_CODED_OUTPUT_STREAM)
                    .addStatement("stream.writeUInt32NoTag(requiredSize)")
                    .addStatement("writeTo(stream)")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("newBuilderForType")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(MESSAGE_LITE.nestedClass("Builder"))
                    .addStatement("throw %T()", NotImplementedError::class.asClassName())
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("toBuilder")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(MESSAGE_LITE.nestedClass("Builder"))
                    .addStatement("throw %T()", NotImplementedError::class.asClassName())
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
                                    .addStatement("return %N(%T(input))", Const.Message.Companion.WrapperDeserializationFunction.NAME, CodedInputStream)
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
