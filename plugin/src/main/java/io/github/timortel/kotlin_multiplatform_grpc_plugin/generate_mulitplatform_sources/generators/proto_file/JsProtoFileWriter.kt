package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.ActualMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.JsOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.ActualRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ActualScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

object JsProtoFileWriter : ActualProtoFileWriter() {

    private val UInt8Array = ClassName("org.khronos.webgl", "Uint8Array")

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator
        get() = ActualScalarMessageMethodGenerator
    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator
        get() = ActualRepeatedMessageMethodGenerator
    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator
        get() = JsOneOfMethodAndClassGenerator
    override val mapMessageMethodGenerator: MapMessageMethodGenerator
        get() = ActualMapMessageMethodGenerator

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
        message: ProtoMessage,
        attr: ProtoMessageField,
        mapType: MapType
    ) {
        builder.apply {
            addCode(
                "%M(%N, %L, %N, ",
                writeMap,
                Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                attr.protoId,
                Const.Message.Attribute.propertyName(message, attr)
            )

            addMapKeyTypeSerializationCode(mapType.keyTypes)
            addCode(", ")
            addMapValueTypeSerializationCode(mapType.valueTypes)
            addCode(")\n")
        }
    }
}