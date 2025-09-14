package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoRegularField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock
import io.github.timortel.kmpgrpc.shared.internal.io.DataType
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatForType
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatMakeTag

/**
 * Adds the deserialization function using CodedOutputStream to the companion object.
 */
class DeserializationFunctionExtension : BaseSerializationExtension() {

    private val wrapperParamName = Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM.name

    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addFunction(
            //The function that builds the message from a stream.
            FunSpec
                .builder(Const.Message.Companion.WrapperDeserializationFunction.NAME)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM.toParamSpec())
                .addParameter(
                    Const.Message.Companion.WrapperDeserializationFunction.EXTENSION_REGISTRY_PARAM
                        .parametrizedBy(message.className)
                        .toParamSpec()
                )
                .returns(message.className)
                .apply {
                    if (sourceTarget is SourceTarget.Actual) {
                        addModifiers(KModifier.ACTUAL)
                        buildWrapperDeserializationFunction(this, message)
                    }
                }
                .build()
        )
    }

    private fun buildWrapperDeserializationFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) {
        // declare unknownFields and extension builder variables
        declareLocalVariable(
            builder = builder,
            fieldName = Const.Message.Companion.WrapperDeserializationFunction.UNKNOWN_FIELDS_LOCAL_VARIABLE,
            type = MUTABLE_LIST.parameterizedBy(unknownField),
            isMutable = false,
            defaultValue = CodeBlock.of("mutableListOf()")
        )

        declareLocalVariable(
            builder = builder,
            fieldName = Const.Message.Companion.WrapperDeserializationFunction.EXTENSION_BUILDER_LOCAL_VARIABLE,
            type = kmExtensionBuilder.parameterizedBy(message.className),
            isMutable = false,
            defaultValue = CodeBlock.of("%T()", kmExtensionBuilder)
        )

        // declare variables for each field
        declareLocalVariablesForFields(builder, message)

        writeDeserializationLoop(builder, message)

        writeReturnStatement(builder, message)
    }

    private fun writeDeserializationLoop(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) {
        val tagLocalFieldName: String = Const.Message.Companion.WrapperDeserializationFunction.TAG_LOCAL_VARIABLE

        builder.apply {
            beginControlFlow("while (true)")
            addStatement(
                "val %N = %N.readTag()",
                tagLocalFieldName,
                wrapperParamName
            )
            addStatement("if (%N == 0) break", tagLocalFieldName)

            beginControlFlow("when (%N)", tagLocalFieldName)

            declareWhenEntriesForMapFields(message)

            declareWhenEntriesForFields(message)

            declareWhenEntriesForOneOfs(message)

            // Unknown field or extension
            addStatement(
                "else -> %M(%N.%N(%N, %N), %N, %N)",
                mergeUnknownFieldOrExtension,
                wrapperParamName,
                "readUnknownFieldOrExtension",
                tagLocalFieldName,
                Const.Message.Companion.WrapperDeserializationFunction.EXTENSION_REGISTRY_PARAM.name,
                Const.Message.Companion.WrapperDeserializationFunction.UNKNOWN_FIELDS_LOCAL_VARIABLE,
                Const.Message.Companion.WrapperDeserializationFunction.EXTENSION_BUILDER_LOCAL_VARIABLE,
            )

            endControlFlow()

            endControlFlow()
        }
    }

    private fun FunSpec.Builder.declareWhenEntriesForOneOfs(message: ProtoMessage) {
        message.oneOfs.forEach { oneOf ->
            oneOf.fields.forEach { field ->
                addCode(buildFieldTagCode(field, field.isPacked))
                addCode(
                    "·->·%N·=·%T(",
                    oneOf.attributeName,
                    field.sealedClassChildName
                )
                addCode(buildReadScalarFieldDataCode(field))
                addCode(")\n")
            }
        }
    }

    private fun FunSpec.Builder.declareWhenEntriesForFields(message: ProtoMessage) {
        message.fields.forEach { field ->
            // https://protobuf.dev/programming-guides/encoding/#repeated
            // Must be able to read both packed and non-packed version
            if (field.cardinality == ProtoFieldCardinality.Repeated && field.type.isPackable) {
                declareWhenEntryForField(field = field, isPacked = true)
                declareWhenEntryForField(field = field, isPacked = false)
            } else {
                declareWhenEntryForField(field = field, isPacked = false)
            }
        }
    }

    private fun FunSpec.Builder.declareWhenEntryForField(field: ProtoMessageField, isPacked: Boolean) {
        addCode(buildFieldTagCode(field, isPacked))

        addCode(" -> ")

        when (field.cardinality) {
            is ProtoFieldCardinality.Singular -> {
                addCode("%N·=·", field.attributeName)
                addCode(buildReadScalarFieldDataCode(field))
                addCode("\n")
            }

            ProtoFieldCardinality.Repeated -> {
                beginControlFlow("{")

                when {
                    field.type is ProtoType.DefType && field.type.isMessage -> {
                        val message = field.type.resolveDeclaration() as ProtoMessage

                        addCode(
                            "%N·+=·%N.%N(%T.Companion, ",
                            field.attributeName,
                            wrapperParamName,
                            "readMessage",
                            field.type.resolve()
                        )

                        addCode(buildExtensionRegistryCodeForMessage(message))

                        addCode(")\n")
                    }

                    isPacked -> {
                        addStatement(
                            "val length·=·%N.readInt32()",
                            wrapperParamName
                        )
                        addStatement(
                            "val limit·=·%N.pushLimit(length)",
                            wrapperParamName
                        )
                        beginControlFlow(
                            "while·(!%N.isAtEnd)·{",
                            wrapperParamName
                        )

                        //Enums need to first get mapped
                        if (field.type.isEnum) {
                            addStatement(
                                "%N += %T.%N(%N.readEnum())",
                                field.attributeName,
                                field.type.resolve(),
                                Const.Enum.GET_ENUM_FOR_FUNCTION_NAME,
                                wrapperParamName
                            )
                        } else {
                            val functionName = getReadScalarFunctionName(field.type)

                            addStatement(
                                "%N·+=·%N.%N()",
                                field.attributeName,
                                wrapperParamName,
                                functionName
                            )
                        }

                        endControlFlow()

                        addStatement("%N.popLimit(limit)", wrapperParamName)
                    }

                    else -> {
                        // not packed
                        if (field.type.isEnum) {
                            addCode(
                                "%N·+=·%T.%N(%N.%N())\n",
                                field.attributeName,
                                field.type.resolve(),
                                Const.Enum.GET_ENUM_FOR_FUNCTION_NAME,
                                wrapperParamName,
                                getReadScalarFunctionName(field.type)
                            )
                        } else {
                            addCode(
                                "%N·+=·%N.%N()\n",
                                field.attributeName,
                                wrapperParamName,
                                getReadScalarFunctionName(field.type)
                            )
                        }
                    }
                }

                endControlFlow()
            }
        }
    }

    private fun FunSpec.Builder.declareWhenEntriesForMapFields(message: ProtoMessage) {
        message.mapFields.forEach { mapField ->
            addCode("%L", wireFormatMakeTag(mapField.number, DataType.MESSAGE, false))

            addCode(" -> ")

            addCode(
                "%N.%N(%N, %T.%N, %T.%N, ",
                wrapperParamName,
                "readMapEntry",
                mapField.attributeName,
                DataType::class.asTypeName(),
                mapField.keyType.wireType.name,
                DataType::class.asTypeName(),
                mapField.valuesType.wireType.name,
            )

            val getDefaultEntry = { type: ProtoType ->
                when (type) {
                    is ProtoType.DefType -> type.defaultValue(ProtoType.MessageDefaultValue.EMPTY)
                    is ProtoType.NonDeclType -> type.defaultValue()
                }
            }

            //Write default values
            addCode(getDefaultEntry(mapField.keyType))
            addCode(", ")
            addCode(getDefaultEntry(mapField.valuesType))
            addCode(", ")
            addCode(buildReadMapFieldDataCode(mapField.keyType))
            addCode(", ")
            addCode(buildReadMapFieldDataCode(mapField.valuesType))
            addCode(")\n")
        }
    }

    private fun writeReturnStatement(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) {
        builder.apply {
            addCode("return %T(", message.className)

            val separator = ",\n"

            val fieldsBlock = (message.fields + message.mapFields + message.oneOfs)
                .joinToCodeBlock(separator = separator) { field ->
                    add(
                        "%N·=·%N",
                        field.attributeName,
                        field.attributeName
                    )
                }

            val unknownFieldsBlock =
                CodeBlock.of(
                    "%N·=·%N",
                    Const.Message.Constructor.UnknownFields.name,
                    Const.Message.Companion.WrapperDeserializationFunction.UNKNOWN_FIELDS_LOCAL_VARIABLE
                )

            val extensionsBlock =
                CodeBlock.of(
                    "%N·=·%N.build()",
                    Const.Message.Constructor.MessageExtensions.name,
                    Const.Message.Companion.WrapperDeserializationFunction.EXTENSION_BUILDER_LOCAL_VARIABLE
                )

            val codeBlocks = listOf(
                fieldsBlock,
                unknownFieldsBlock
            ) + if (message.isExtendable) listOf(extensionsBlock) else emptyList()

            addCode(codeBlocks.joinCodeBlocks(separator))

            addCode(")\n")
        }
    }

    private fun declareLocalVariablesForFields(builder: FunSpec.Builder, message: ProtoMessage) {
        message.fields.forEach { field ->
            when (field.cardinality) {
                is ProtoFieldCardinality.Singular -> {
                    val type = if (field.needsIsSetProperty)
                        field.type.resolve().copy(nullable = true)
                    else field.type.resolve()

                    val defaultValue = if (field.needsIsSetProperty) {
                        CodeBlock.of("null")
                    } else field.type.defaultValue()

                    declareLocalVariable(
                        builder,
                        field.attributeName,
                        type,
                        true,
                        defaultValue
                    )
                }

                ProtoFieldCardinality.Repeated -> {
                    declareLocalVariable(
                        builder,
                        field.attributeName,
                        MUTABLE_LIST.parameterizedBy(field.type.resolve()),
                        false,
                        CodeBlock.of("mutableListOf()")
                    )
                }
            }
        }

        message.mapFields.forEach { field ->
            declareLocalVariable(
                builder,
                field.attributeName,
                MUTABLE_MAP.parameterizedBy(field.keyType.resolve(), field.valuesType.resolve()),
                false,
                CodeBlock.of("mutableMapOf()")
            )
        }

        message.oneOfs.forEach { oneOf ->
            declareLocalVariable(
                builder,
                oneOf.attributeName,
                oneOf.sealedClassName,
                true,
                CodeBlock.of("%T", oneOf.sealedClassNameNotSet)
            )
        }
    }

    private fun declareLocalVariable(
        builder: FunSpec.Builder,
        fieldName: String,
        type: TypeName,
        isMutable: Boolean,
        defaultValue: CodeBlock
    ) {
        builder.addCode(
            if (isMutable) "var %N: %T = " else "val %N: %T = ",
            fieldName,
            type
        )

        builder.addCode(defaultValue)
        builder.addCode("\n")
    }

    private fun buildFieldTagCode(field: ProtoRegularField, isPacked: Boolean): CodeBlock {
        return CodeBlock.of("%L", wireFormatMakeTag(field.number, wireFormatForType(field.type.wireType, isPacked)))
    }

    /**
     * @return a CodeBlock that reads the data for [field] under the assumption that it is a scalar field
     */
    private fun buildReadScalarFieldDataCode(field: ProtoRegularField): CodeBlock {
        return when (val type = field.type) {
            is ProtoType.NonDeclType -> {
                CodeBlock.of(
                    "%N.%N()",
                    wrapperParamName,
                    getReadScalarFunctionName(field.type)
                )
            }

            is ProtoType.DefType -> {
                when (val decl = type.resolveDeclaration()) {
                    is ProtoMessage -> {
                        CodeBlock.builder()
                            .add(
                                "%N.%N(%T.Companion, ",
                                wrapperParamName,
                                "readMessage",
                                field.type.resolve()
                            )
                            .add(buildExtensionRegistryCodeForMessage(decl))
                            .add(")")
                            .build()
                    }

                    is ProtoEnum -> {
                        CodeBlock.of(
                            "%T.%N(%N.readEnum())",
                            field.type.resolve(),
                            Const.Enum.GET_ENUM_FOR_FUNCTION_NAME,
                            wrapperParamName
                        )
                    }
                }
            }
        }
    }

    private fun buildReadMapFieldDataCode(type: ProtoType): CodeBlock {
        return when (type) {
            is ProtoType.NonDeclType -> {
                CodeBlock.of(
                    "{·%N()·}",
                    getReadScalarFunctionName(type)
                )
            }

            is ProtoType.DefType -> {
                when (val decl = type.resolveDeclaration()) {
                    is ProtoMessage -> {
                        CodeBlock.builder()
                            .add(
                                "{·%N(%T.Companion, ",
                                "readMessage",
                                type.resolve()
                            )
                            .add(buildExtensionRegistryCodeForMessage(decl))
                            .add(")}")
                            .build()
                    }

                    is ProtoEnum -> {
                        CodeBlock.of(
                            "{·%T.%N(readEnum())·}",
                            type.resolve(),
                            Const.Enum.GET_ENUM_FOR_FUNCTION_NAME
                        )
                    }
                }
            }
        }
    }

    private fun getReadScalarFunctionName(protoType: ProtoType): String {
        return when (protoType) {
            ProtoType.DoubleType -> "readDouble"
            ProtoType.FloatType -> "readFloat"
            ProtoType.Int32Type -> "readInt32"
            ProtoType.Int64Type -> "readInt64"
            ProtoType.UInt32Type -> "readUInt32"
            ProtoType.UInt64Type -> "readUInt64"
            ProtoType.SInt32Type -> "readSInt32"
            ProtoType.SInt64Type -> "readSInt64"
            ProtoType.Fixed32Type -> "readFixed32"
            ProtoType.Fixed64Type -> "readFixed64"
            ProtoType.SFixed32Type -> "readSFixed32"
            ProtoType.SFixed64Type -> "readSFixed64"
            ProtoType.BoolType -> "readBool"
            ProtoType.StringType -> "readString"
            ProtoType.BytesType -> "readBytes"
            is ProtoType.DefType -> {
                when (protoType.declType) {
                    ProtoType.DefType.DeclarationType.MESSAGE -> "readMessage"
                    ProtoType.DefType.DeclarationType.ENUM -> "readEnum"
                }
            }
        }
    }

    private fun buildExtensionRegistryCodeForMessage(message: ProtoMessage): CodeBlock {
        return if (message.isExtendable) {
            CodeBlock.of(
                "%M",
                message.className.nestedClass("Companion").member(Const.Message.Companion.defaultExtensionRegistryProperty.name)
            )
        } else {
            CodeBlock.of("%T.empty()", kmExtensionRegistry)
        }
    }
}
