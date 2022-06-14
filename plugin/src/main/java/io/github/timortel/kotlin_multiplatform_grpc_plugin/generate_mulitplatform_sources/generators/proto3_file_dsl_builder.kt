package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.DslBuilder
import java.io.File

fun writeDSLBuilder(protoFile: ProtoFile, dslBuilder: DslBuilder, outputDir: File) {
    val builder = FileSpec
        .builder(protoFile.pkg, protoFile.fileNameWithoutExtension + "_dsl_builder")

    dslBuilder.generateDslBuilders(protoFile, builder)

    builder
        .build()
        .writeTo(outputDir)
}