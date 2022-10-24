package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedInputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.CodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.IosJvmMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.IosJvmOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.IosJvmRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.IosJvmScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator
import java.nio.ByteBuffer

class JvmProtoFileWriter(protoFile: ProtoFile) : IosJvmProtoFileWriteBase(protoFile) {

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator = IosJvmScalarMessageMethodGenerator

    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator = IosJvmRepeatedMessageMethodGenerator

    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator = IosJvmOneOfMethodAndClassGenerator

    override val mapMessageMethodGenerator: MapMessageMethodGenerator = IosJvmMapMessageMethodGenerator

    override val serializeFunctionCode: CodeBlock
        get() = CodeBlock.builder()
            .addStatement("val data = ByteArray(requiredSize)")
            .addStatement(
                "val stream = %T(%T.newInstance(%T.wrap(data)))",
                CodedOutputStream,
                ClassName("com.google.protobuf", "CodedOutputStream"),
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
                ClassName("com.google.protobuf", "CodedInputStream"),
            )
            .addStatement(
                "return %N(stream)",
                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
            )
            .build()
}