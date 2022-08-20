package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*

abstract class DslBuilder(private val isActual: Boolean) {

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

        val dslBuilderClassName = "KM${message.capitalizedName}DSL"
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
                    message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                        when (attr.attributeType) {
                            is Scalar -> {
                                addProperty(
                                    PropertySpec
                                        .builder(
                                            Const.DSL.Attribute.Scalar.propertyName(attr),
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
                                            Const.DSL.Attribute.Repeated.propertyName(attr),
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
                                            Const.DSL.Attribute.Map.propertyName(attr),
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

                    message.oneOfs.forEach { oneOf ->
                        addProperty(
                            PropertySpec
                                .builder(
                                    Const.DSL.OneOf.propertyName(message, oneOf),
                                    Const.Message.OneOf.parentSealedClassName(message, oneOf),
                                    modifier
                                )
                                .mutable()
                                .apply {
                                    if (isActual) {
                                        initializer(oneOf.defaultValue(message))
                                    }
                                }
                                .build()
                        )
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
                    .builder("km${message.capitalizedName}")
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

    abstract fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage)
}