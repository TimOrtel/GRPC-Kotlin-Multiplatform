package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.ActualMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.IosJvmOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.ActualRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ActualScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

class IosProtoFileWriter(protoFile: ProtoFile) : IosJvmProtoFileWriteBase(protoFile) {

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator
        get() = ActualScalarMessageMethodGenerator
    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator
        get() = ActualRepeatedMessageMethodGenerator
    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator
        get() = IosJvmOneOfMethodAndClassGenerator
    override val mapMessageMethodGenerator: MapMessageMethodGenerator
        get() = ActualMapMessageMethodGenerator


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