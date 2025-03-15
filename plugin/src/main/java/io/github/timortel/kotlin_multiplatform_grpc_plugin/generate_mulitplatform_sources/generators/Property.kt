package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

data class Property(val name: String, val type: TypeName) {
    fun toParamSpec(vararg modifiers: KModifier): ParameterSpec = ParameterSpec.builder(name, type, *modifiers).build()
}