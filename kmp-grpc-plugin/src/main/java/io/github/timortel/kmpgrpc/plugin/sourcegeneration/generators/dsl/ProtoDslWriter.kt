package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.DefaultAnnotations
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize

abstract class ProtoDslWriter(private val isActual: Boolean) {

    fun generateDslBuilderFile(protoFile: ProtoFile): FileSpec {
        val builder = FileSpec
            .builder(protoFile.javaPackage, protoFile.javaFileName + "Dsl")
            .addAnnotation(DefaultAnnotations.SuppressDeprecation)
            .addAnnotation(DefaultAnnotations.OptIntoKmpGrpcInternalApi)

        generateDslBuilders(protoFile, builder)

        return builder.build()
    }

    private fun generateDslBuilders(protoFile: ProtoFile, builder: FileSpec.Builder) {
        protoFile.messages.forEach { message ->
            writeDslBuilder(
                null,
                message,
                addType = builder::addType,
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
        addTopLevelFunction: (FunSpec) -> Unit
    ) {
        //We only need the modifiers in actual declarations, and in top level expect declarations
        val modifier: List<KModifier> =
            if (isActual) listOf(KModifier.ACTUAL) else if (currentClass != null) emptyList() else listOf(KModifier.EXPECT)

        val dslBuilderClassName = "${message.name.capitalize()}DSL"
        val dslBuilderClassType =
            currentClass?.nestedClass(dslBuilderClassName) ?: ClassName(message.file.javaPackage, dslBuilderClassName)

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
                    message.fields.forEach { field ->
                        when (field.cardinality) {
                            is ProtoFieldCardinality.Singular -> {
                                addProperty(
                                    PropertySpec
                                        .builder(
                                            field.attributeName,
                                            field.type.resolve().copy(nullable = true)
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

                            is ProtoFieldCardinality.Repeated -> {
                                addProperty(
                                    PropertySpec
                                        .builder(
                                            field.attributeName,
                                            MUTABLE_LIST.parameterizedBy(field.type.resolve())
                                        ).apply {
                                            if (isActual) {
                                                initializer("%M()", MemberName("kotlin.collections", "mutableListOf"))
                                            }
                                        }
                                        .addModifiers(modifier)
                                        .build()
                                )
                            }
                        }
                    }

                    message.mapFields.forEach { field ->
                        addProperty(
                            PropertySpec
                                .builder(
                                    name = field.attributeName,
                                    type = MUTABLE_MAP.parameterizedBy(
                                        field.keyType.resolve(),
                                        field.valuesType.resolve()
                                    )
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

                    message.oneOfs.forEach { oneOf ->
                        addProperty(
                            PropertySpec
                                .builder(
                                    oneOf.attributeName,
                                    oneOf.sealedClassName,
                                    modifier
                                )
                                .mutable()
                                .apply {
                                    if (isActual) {
                                        initializer("%T", oneOf.sealedClassNameNotSet)
                                    }
                                }
                                .build()
                        )
                    }
                }
                .addFunction(
                    FunSpec
                        .builder(Const.DSL.BUILD_FUNCTION_NAME)
                        .addModifiers(modifier)
                        .returns(message.className)
                        .apply { if (isActual) modifyBuildFunction(this, message) }
                        .build()
                )
                .apply {
                    if (message.messages.isNotEmpty()) {
                        message.messages.forEach { childMessage ->
                            writeDslBuilder(
                                currentClass = dslBuilderClassType,
                                message = childMessage,
                                addType = this::addType,
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
                    .builder(message.dslBuildFunction)
                    .addModifiers(KModifier.INLINE)
                    .addParameter(
                        "builderDsl",
                        LambdaTypeName.get(
                            receiver = dslBuilderClassType,
                            returnType = UNIT
                        )
                    )
                    .returns(message.className)
                    .addStatement("return %T().apply(builderDsl).build()", dslBuilderClassType)
                    .build()
            )
        }
    }

    abstract fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage)
}
