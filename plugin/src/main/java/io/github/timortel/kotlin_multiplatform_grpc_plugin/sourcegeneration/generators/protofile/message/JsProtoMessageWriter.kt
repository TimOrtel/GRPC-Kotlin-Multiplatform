package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.oneof.JsProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.JsProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.*

object JsProtoMessageWriter : ActualProtoMessageWriter() {

    private val UInt8Array = ClassName("org.khronos.webgl", "Uint8Array")

    override val protoServiceWriter: ProtoServiceWriter = JsProtoServiceWriter
    override val protoOneOfWriter: ProtoOneOfWriter = JsProtoOneOfWriter

    override val serializeFunctionCode: CodeBlock
        get() = CodeBlock.builder()
            .addStatement("val writer = %T()", JSPB_BINARY_WRITER)
            .addStatement("val stream = %T(writer)", CodedOutputStream)
            .addStatement("serialize(stream)")
            .addStatement("return writer.getResultBuffer()")
            .build()

    override val serializedDataType: TypeName = UInt8Array

    override val deserializeFunctionCode: CodeBlock
        get() = CodeBlock
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

    override fun buildMapAttributeSerializeCode(
        builder: FunSpec.Builder,
        field: ProtoMapField
    ) {
        builder.apply {
            addCode(
                "%M(%N, %L, %N, ",
                writeMap,
                Const.Message.SerializeFunction.STREAM_PARAM,
                field.number,
                field.fieldName
            )

            addMapKeyTypeSerializationCode(field.keyType)
            addCode(", ")
            addMapValueTypeSerializationCode(field.valuesType)
            addCode(")\n")
        }
    }
}
