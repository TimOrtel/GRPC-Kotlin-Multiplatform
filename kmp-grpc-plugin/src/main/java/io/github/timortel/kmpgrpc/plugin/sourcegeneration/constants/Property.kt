package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

sealed interface Property {

    val name: String
    val type: TypeName

    fun toPropertySpecBuilder(vararg modifiers: KModifier): PropertySpec.Builder = PropertySpec.builder(name, type, *modifiers)
    fun toPropertySpec(vararg modifiers: KModifier): PropertySpec = toPropertySpecBuilder(*modifiers).build()

    fun toParamSpecBuilder(vararg modifiers: KModifier): ParameterSpec.Builder = ParameterSpec.builder(name, type, *modifiers)
    fun toParamSpec(vararg modifiers: KModifier): ParameterSpec = toParamSpecBuilder(*modifiers).build()

    companion object {
        fun of(name: String, type: TypeName) = TypeNameProperty(name, type)
        fun of(name: String, type: ClassName) = ClassNameProperty(name, type)
        fun of(name: String, type: ParameterizedTypeName) = ParametrizedTypeNameProperty(name, type)
    }

    data class TypeNameProperty(override val name: String, override val type: TypeName) : Property
    data class ClassNameProperty(override val name: String, override val type: ClassName) : Property {
        fun parametrizedBy(vararg types: TypeName): ParametrizedTypeNameProperty {
            return ParametrizedTypeNameProperty(name, type.parameterizedBy(*types))
        }
    }
    data class ParametrizedTypeNameProperty(override val name: String, override val type: ParameterizedTypeName) : Property
}
