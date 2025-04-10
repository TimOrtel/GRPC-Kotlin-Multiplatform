package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

data class Property(val name: String, val type: TypeName) {
    fun toPropertySpecBuilder(vararg modifiers: KModifier): PropertySpec.Builder = PropertySpec.builder(name, type, *modifiers)
    fun toPropertySpec(vararg modifiers: KModifier): PropertySpec = toPropertySpecBuilder(*modifiers).build()

    fun toParamSpecBuilder(vararg modifiers: KModifier): ParameterSpec.Builder = ParameterSpec.builder(name, type, *modifiers)
    fun toParamSpec(vararg modifiers: KModifier): ParameterSpec = toParamSpecBuilder(*modifiers).build()
}
