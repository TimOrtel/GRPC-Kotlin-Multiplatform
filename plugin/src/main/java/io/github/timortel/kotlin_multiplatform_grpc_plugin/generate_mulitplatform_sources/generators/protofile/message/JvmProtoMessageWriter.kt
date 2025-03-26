package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.message

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedInputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.JvmProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.ProtoServiceWriter
import java.nio.ByteBuffer

object JvmProtoMessageWriter : IosJvmProtoMessageWriteBase() {

    private const val PACKAGE_PROTOBUF = "com.google.protobuf"

    private val MESSAGE_LITE = ClassName(PACKAGE_PROTOBUF, "MessageLite")
    private val PROTO_CODED_OUTPUT_STREAM = ClassName(PACKAGE_PROTOBUF, "CodedOutputStream")
    private val PROTO_CODED_INPUT_STREAM = ClassName(PACKAGE_PROTOBUF, "CodedInputStream")

    private val BYTE_STRING = ClassName(PACKAGE_PROTOBUF, "ByteString")

    override val protoServiceWriter: ProtoServiceWriter = JvmProtoServiceWriter

    override val serializeFunctionCode: CodeBlock
        get() = CodeBlock.builder()
            .addStatement("val data = ByteArray(requiredSize)")
            .addStatement(
                "val stream = %T(%T.newInstance(%T.wrap(data)))",
                CodedOutputStream,
                PROTO_CODED_OUTPUT_STREAM,
                ByteBuffer::class.asClassName()
            )
            .addStatement("serialize(stream)")
            .addStatement("return data")
            .build()

    override val serializedDataType: ClassName = ByteArray::class.asClassName()

    override val deserializeFunctionCode: CodeBlock
        get() = CodeBlock
            .builder()
            .addStatement(
                "val stream = %T(%T.newInstance(data))",
                CodedInputStream,
                PROTO_CODED_INPUT_STREAM,
            )
            .addStatement(
                "return %N(stream)",
                Const.Message.Companion.WrapperDeserializationFunction.NAME
            )
            .build()

    override val additionalSuperinterfaces: List<TypeName> = listOf(MESSAGE_LITE)

    override fun applyToClass(
        builder: TypeSpec.Builder,
        message: io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage
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
