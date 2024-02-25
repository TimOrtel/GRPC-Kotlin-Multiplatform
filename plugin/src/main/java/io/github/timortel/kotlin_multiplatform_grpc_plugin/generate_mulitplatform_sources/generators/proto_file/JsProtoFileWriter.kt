package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.Dynamic
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedInputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.JSPB_BINARY_READER
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.JSPB_BINARY_WRITER
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.ActualMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.JsOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.ActualRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ActualScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.writeMap

class JsProtoFileWriter(protoFile: ProtoFile) : ActualProtoFileWriter(protoFile) {

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

    override val serializedDataType: TypeName = Dynamic

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
        attr: ProtoMessageAttribute,
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