package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.IosProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.ProtoServiceWriter

object IosProtoMessageWriter : IosJvmProtoMessageWriteBase() {

    override val protoServiceWriter: ProtoServiceWriter = IosProtoServiceWriter

    override val serializeFunctionCode: CodeBlock
        get() = CodeBlock.builder()
            .addStatement("val data = %T().apply { setLength(requiredSize.toULong()) }", NSMutableData)
            .addStatement(
                "val stream = %T(%T(data))",
                CodedOutputStream,
                ClassName("cocoapods.Protobuf", "GPBCodedOutputStream")
            )
            .addStatement("serialize(stream)")
            .addStatement("return data")
            .build()

    override val serializedDataType: ClassName = NSData

    override val deserializeFunctionCode: CodeBlock
        get() = CodeBlock
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