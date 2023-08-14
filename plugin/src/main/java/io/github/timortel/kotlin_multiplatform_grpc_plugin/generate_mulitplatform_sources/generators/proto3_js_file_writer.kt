package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.JSImpl
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.MessageDeserializer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.common.JsCommonFunctionGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.JsDslBuilder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.JsProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.IosJvmDslBuilder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmMessage
import java.io.File

private val jspb = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "JSPB")

const val objPropertyName = "obj"

fun writeJsFiles(protoFile: ProtoFile, jsOutputDir: File) {
    JsProtoFileWriter(protoFile).writeFile(jsOutputDir)

    writeDSLBuilder(protoFile, IosJvmDslBuilder, jsOutputDir)
}