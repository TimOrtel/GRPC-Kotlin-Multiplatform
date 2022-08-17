package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.MapMapper

abstract class SubDslBuilder(isActual: Boolean) : DslBuilder(isActual) {

    protected abstract val mapMapper: MapMapper

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        val buildParam = initializeBuilder(builder, message)

        builder.apply {
            message.attributes.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar -> {
                        val attrName = Const.DSL.Attribute.Scalar.attrName(attr)
                        addStatement(
                            "val %N = %N",
                            attrName,
                            attrName
                        )
                        beginControlFlow("if (%N != null)", attrName)

                        if (attr.types.isEnum) {
                            setEnumValue(builder, attrName, message, attr, buildParam)
                        } else {
                            setScalarValue(builder, attrName, message, attr, buildParam)
                        }

                        endControlFlow()
                    }
                    is Repeated -> {
                        if (attr.types.isEnum) {
                            addAllEnumValues(builder, message, attr, buildParam)
                        } else {
                            addAllValues(builder, message, attr, buildParam)
                        }
                    }
                    is MapType -> {
                        addCode(
                            mapMapper.mapMap(
                                buildParam,
                                CodeBlock.of(Const.DSL.Attribute.Map.attrName(attr)),
                                message,
                                attr,
                                attr.attributeType
                            )
                        )
                    }
                }
            }
        }

        returnPlatformType(builder, message, buildParam)
    }

    /**
     * Initialize the platform dependent builder
     * @return the name of the builder variable
     */
    protected abstract fun initializeBuilder(builder: FunSpec.Builder, message: ProtoMessage): String

    /**
     * Add the code that returns the final platform dependent KM type
     */
    protected abstract fun returnPlatformType(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        builderVariableName: String
    )

    /**
     * Add code to set the enum value stored in variableName.
     */
    protected abstract fun setEnumValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    )

    /**
     * Add code to set the non-enum scalar value stored in variable name
     */
    protected abstract fun setScalarValue(
        builder: FunSpec.Builder,
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    )

    /**
     * Add all non-enums to the list in the builder
     */
    protected abstract fun addAllValues(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    )

    /**
     * Add all enums to the list in the builder
     */
    protected abstract fun addAllEnumValues(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        builderVariable: String
    )
}