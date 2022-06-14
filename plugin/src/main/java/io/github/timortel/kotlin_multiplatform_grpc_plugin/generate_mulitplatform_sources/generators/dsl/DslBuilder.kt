package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper.MapMapper
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*

abstract class DslBuilder(private val isActual: Boolean) {

    protected abstract val mapMapper: MapMapper

    fun generateDslBuilders(protoFile: ProtoFile, builder: FileSpec.Builder) {
        protoFile.messages.forEach { message ->
            writeDslBuilder(
                null,
                message,
                addType = builder::addType,
                addFunction = builder::addFunction,
                addTopLevelFunction = builder::addFunction
            )
        }
    }

    /**
     * @param addTopLevelFunction the kmType builder functions are always added on top level, because they contain code, so they cannot be in the nested expect classes.
     */
    private fun writeDslBuilder(
        currentClass: ClassName?,
        message: ProtoMessage,
        addType: (TypeSpec) -> Unit,
        addFunction: (FunSpec) -> Unit,
        addTopLevelFunction: (FunSpec) -> Unit
    ) {
        //We only need the modifiers in actual declarations, and in top level expect declarations
        val modifier: List<KModifier> =
            if (isActual) listOf(KModifier.ACTUAL) else if (currentClass != null) emptyList() else listOf(KModifier.EXPECT)

        val dslBuilderClassName = "KM${message.name.capitalize()}DSL"
        val dslBuilderClassType =
            currentClass?.nestedClass(dslBuilderClassName) ?: ClassName(message.pkg, dslBuilderClassName)
        addType(
            TypeSpec
                .classBuilder(dslBuilderClassName)
                .addModifiers(modifier)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .apply {
                            if (isActual) addModifiers(KModifier.ACTUAL)
                        }
                        .build()
                )
                .apply {
                    message.attributes.forEach { attr ->
                        when (attr.attributeType) {
                            is Scalar -> {
                                addProperty(
                                    PropertySpec
                                        .builder(
                                            Const.DSL.Attribute.Scalar.attrName(attr),
                                            attr.commonType.copy(nullable = true)
                                        )
                                        .addModifiers(modifier)
                                        .mutable()
                                        .apply {
                                            if (isActual) {
                                                initializer("null")
                                            }
                                        }
                                        .build()
                                )
                            }
                            is Repeated -> {
                                addProperty(
                                    PropertySpec
                                        .builder(
                                            Const.DSL.Attribute.Repeated.attrName(attr),
                                            ClassName(
                                                "kotlin.collections",
                                                "MutableList"
                                            ).parameterizedBy(attr.commonType)
                                        ).apply {
                                            if (isActual) {
                                                initializer("%M()", MemberName("kotlin.collections", "mutableListOf"))
                                            }
                                        }
                                        .addModifiers(modifier)
                                        .build()
                                )
                            }
                            is MapType -> {
                                addProperty(
                                    PropertySpec
                                        .builder(
                                            Const.DSL.Attribute.Map.attrName(attr),
                                            attr.attributeType.commonMutableMapType
                                        )
                                        .addModifiers(modifier)
                                        .apply {
                                            if (isActual) {
                                                initializer("mutableMapOf()")
                                            }
                                        }
                                        .build()
                                )
                            }
                        }
                    }
                }
                .addFunction(
                    FunSpec
                        .builder(Const.DSL.buildFunctionName)
                        .addModifiers(modifier)
                        .returns(message.commonType)
                        .apply { if (isActual) modifyBuildFunction(this, message) }
                        .build()
                )
                .apply {
                    if (message.children.isNotEmpty()) {

                        message.children.forEach { childMessage ->
                            writeDslBuilder(
                                currentClass = dslBuilderClassType,
                                message = childMessage,
                                addType = this::addType,
                                addFunction = this::addFunction,
                                addTopLevelFunction = addTopLevelFunction
                            )
                        }
                    }

                }
                .build()
        )

        //This method is only needed once
        if (!isActual) {
            addTopLevelFunction(
                FunSpec
                    .builder("km${message.name.capitalize()}")
                    .addModifiers(KModifier.INLINE)
                    .addParameter(
                        "builderDsl",
                        LambdaTypeName.get(
                            receiver = dslBuilderClassType,
                            returnType = Unit::class.asTypeName()
                        )
                    )
                    .returns(message.commonType)
                    .addStatement("return %T().apply(builderDsl).build()", dslBuilderClassType)
                    .build()
            )
        }
    }

    private fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
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