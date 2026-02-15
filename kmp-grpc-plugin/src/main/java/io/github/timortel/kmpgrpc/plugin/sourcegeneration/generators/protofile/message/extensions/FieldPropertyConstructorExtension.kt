package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMessageExtensions
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.MessageConstructorCallWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType

object FieldPropertyConstructorExtension : MessageWriterExtension {

    override fun applyToConstructor(builder: FunSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        addConstructorParameters(
            builder = builder,
            message = message,
            sourceTarget = sourceTarget,
            type = ProtoMessageField.ConstructorParameterType.CONSTRUCTOR
        )
    }

    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        addCompanionObjectBuildFunction(
            name = "invoke",
            type = ProtoMessageField.ConstructorParameterType.CREATE,
            builder = builder,
            message = message,
            sourceTarget = sourceTarget,
            modifiers = listOf(KModifier.OPERATOR)
        )

        addCompanionObjectBuildFunction(
            name = "createPartial",
            type = ProtoMessageField.ConstructorParameterType.CREATE_PARTIAL,
            builder = builder,
            message = message,
            sourceTarget = sourceTarget,
            modifiers = emptyList()
        )
    }

    private fun addCompanionObjectBuildFunction(
        name: String,
        type: ProtoMessageField.ConstructorParameterType,
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        sourceTarget: SourceTarget,
        modifiers: List<KModifier>
    ) {
        val isActual = sourceTarget is SourceTarget.Actual

        builder.addFunction(
            FunSpec.builder(name)
                .addModifiers(modifiers)
                .returns(message.className)
                .apply {
                    addConstructorParameters(
                        builder = this,
                        message = message,
                        sourceTarget = sourceTarget,
                        type = type
                    )

                    if (message.isExtendable) {
                        addParameter(
                            Const.Message.Constructor.MessageExtensions
                                .parametrizedBy(message.className)
                                .toParamSpecBuilder()
                                .apply {
                                    if (!isActual) defaultValue(CodeBlock.of("%T()", kmMessageExtensions))
                                }
                                .build()
                        )
                    }

                    addParameter(Const.Message.Constructor.UnknownFields
                        .toParamSpecBuilder()
                        .apply {
                            if (!isActual) defaultValue("emptyList()")
                        }
                        .build()
                    )

                    if (isActual) {
                        addModifiers(KModifier.ACTUAL)

                        addCode("return ")
                        addCode(
                            MessageConstructorCallWriter.getConstructorCallCode(
                                message = message,
                                type = MessageConstructorCallWriter.ConstructorType.DIRECT,
                                getFieldParameter = { field -> CodeBlock.of("%N", field.codeName) },
                                getMapFieldParameter = { field -> CodeBlock.of("%N", field.codeName) },
                                getOneOfFieldParameter = { field -> CodeBlock.of("%N", field.codeName) },
                                getUnknownFieldsParameter = {
                                    CodeBlock.of(
                                        "%N",
                                        Const.Message.Constructor.UnknownFields.name
                                    )
                                },
                                getExtensionParameter = {
                                    CodeBlock.of(
                                        "%N",
                                        Const.Message.Constructor.MessageExtensions.name
                                    )
                                },
                            )
                        )
                    }
                }
                .build()
        )
    }

    private fun addConstructorParameters(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        sourceTarget: SourceTarget,
        type: ProtoMessageField.ConstructorParameterType
    ) {
        val isActual = sourceTarget is SourceTarget.Actual

        val addDefaultValues = when (type) {
            ProtoMessageField.ConstructorParameterType.CONSTRUCTOR -> false
            ProtoMessageField.ConstructorParameterType.CREATE, ProtoMessageField.ConstructorParameterType.CREATE_PARTIAL -> true
        }

        //one of attributes do not get a parameter, as they get the one of parameter
        message.fields.forEach { field ->
            when (field.cardinality) {
                is ProtoFieldCardinality.Singular -> {
                    val isParamNullable = field.isConstructorParameterNullable(type)

                    val type = if (isParamNullable) field.type.resolve().copy(nullable = true)
                    else field.type.resolve()

                    builder.addParameter(
                        ParameterSpec
                            .builder(field.codeName, type)
                            .apply {
                                if (!isActual && addDefaultValues) {
                                    defaultValue(
                                        // If the field needs a isSet property, then the constructor must pass null by default
                                        if (isParamNullable) {
                                            CodeBlock.of("null")
                                        } else {
                                            field.type.defaultValue(
                                                messageDefaultValue = ProtoType.MessageDefaultValue.EMPTY
                                            )
                                        }
                                    )
                                }
                            }
                            .build()
                    )
                }

                ProtoFieldCardinality.Repeated -> {
                    builder.addParameter(
                        ParameterSpec
                            .builder(field.codeName, LIST.parameterizedBy(field.type.resolve()))
                            .apply {
                                if (!isActual && addDefaultValues) defaultValue("emptyList()")
                            }
                            .build()
                    )
                }
            }
        }

        message.mapFields.forEach { mapField ->
            builder.addParameter(
                ParameterSpec
                    .builder(
                        mapField.codeName,
                        MAP.parameterizedBy(
                            mapField.keyType.resolve(),
                            mapField.valuesType.resolve(),
                        )
                    )
                    .apply {
                        if (!isActual && addDefaultValues) defaultValue("emptyMap()")
                    }
                    .build()
            )
        }

        message.oneOfs.forEach { oneOf ->
            builder.addParameter(
                ParameterSpec
                    .builder(
                        oneOf.codeName,
                        oneOf.sealedClassName
                    )
                    .apply {
                        if (!isActual && addDefaultValues) defaultValue("%T", oneOf.sealedClassNameNotSet)
                    }
                    .build()
            )
        }
    }
}
