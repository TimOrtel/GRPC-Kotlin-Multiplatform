package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage

object CommonDslBuilder : DslBuilder(false) {

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) = Unit
}