package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.JvmProtoMessageWriter.PROTO_CODED_INPUT_STREAM
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

/**
 * Adds the function that deserializes the message from the native byte format, e.g. ByteArray, NSData
 */
class NativeDeserializationFunctionExtension : BaseSerializationExtension() {

    companion object {
        private val UInt8Array = ClassName("org.khronos.webgl", "Uint8Array")
    }

    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        if (sourceTarget is SourceTarget.Actual) {
            builder.addFunction(
                // The function that builds the message from NSData
                FunSpec
                    .builder(Const.Message.Companion.DataDeserializationFunction.NAME)
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        Const.Message.Companion.DataDeserializationFunction.DATA_PARAM,
                        getSerializedDataType(sourceTarget)
                    )
                    .addCode(
                        getDeserializeFunctionCode(sourceTarget)
                    )
                    .returns(message.className)
                    .build()
            )
        }
    }

    private fun getSerializedDataType(sourceTarget: SourceTarget.Actual): TypeName {
        return when (sourceTarget) {
            SourceTarget.Ios -> NSData
            SourceTarget.Js -> UInt8Array
            SourceTarget.Jvm -> BYTE_ARRAY
        }
    }

    private fun getDeserializeFunctionCode(sourceTarget: SourceTarget.Actual): CodeBlock {
        return when (sourceTarget) {
            SourceTarget.Ios -> {
                CodeBlock
                    .builder()
                    .addStatement(
                        "val stream = %T(%T(data))",
                        CodedInputStream,
                        GPBCodedInputStream
                    )
                    .addStatement(
                        "return %N(stream)",
                        Const.Message.Companion.WrapperDeserializationFunction.NAME
                    )
                    .build()
            }
            SourceTarget.Js -> {
                CodeBlock
                    .builder()
                    .addStatement(
                        "val stream = %T(%T(data))",
                        CodedInputStream,
                        JSPB_BINARY_READER
                    )
                    .addStatement(
                        "return %N(stream)",
                        Const.Message.Companion.WrapperDeserializationFunction.NAME
                    )
                    .build()
            }
            SourceTarget.Jvm -> {
                CodeBlock
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
            }
        }
    }
}
