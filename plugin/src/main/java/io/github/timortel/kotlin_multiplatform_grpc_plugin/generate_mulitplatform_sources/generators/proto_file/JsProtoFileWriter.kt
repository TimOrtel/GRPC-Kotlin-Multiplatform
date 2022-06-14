package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.JsMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.JsOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.JsRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.JsScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

class JsProtoFileWriter(private val protoFile: ProtoFile) : ProtoFileWriter(protoFile, true), DefaultChildClassName {
    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator = JsScalarMessageMethodGenerator

    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator = JsRepeatedMessageMethodGenerator

    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator = JsOneOfMethodAndClassGenerator

    override val mapMessageMethodGenerator: MapMessageMethodGenerator = JsMapMessageMethodGenerator

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        val paramName = Const.Message.Constructor.JS.PARAM_IMPL

        builder.addProperty(
            PropertySpec
                .builder(paramName, message.jsType, KModifier.PUBLIC)
                .initializer(paramName)
                .build()
        )

        builder.primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(paramName, message.jsType)
                .build()
        )
    }

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)
}