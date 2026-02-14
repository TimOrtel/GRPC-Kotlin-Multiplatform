package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.MessageConstructorCallWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoRegularField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
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
                "else -> if·(!%M(%N.%N(%N, %N), %N, %N))·break",
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

                beginControlFlow("·->·")

                addCode(
                    buildAssignScalarFieldValueCodeBlock(
                        type = field.type,
                        file = field.file,
                        fieldNumber = field.number,
                        variableName = oneOf.attributeName,
                        assignMode = "=",
                        constructType = {
                            add("%T(", field.sealedClassChildName)
                            it()
                            add(")")
                        }
                    )
                )

                endControlFlow()
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

    private fun buildAssignScalarFieldValueCodeBlock(
        type: ProtoType,
        file: ProtoFile,
        fieldNumber: Int,
        variableName: String,
        assignMode: String,
        constructType: CodeBlock.Builder.(insertValue: CodeBlock.Builder.() -> Unit) -> Unit
    ): CodeBlock {
        val enumNumberVar =
            Const.Message.Companion.WrapperDeserializationFunction.ENUM_NUMBER_VALUE_LOCAL_VARIABLE
        val enumVar = Const.Message.Companion.WrapperDeserializationFunction.ENUM_VALUE_LOCAL_VARIABLE

        val isClosedEnum = isClosedEnum(type, file)

        return CodeBlock.builder().apply {
            if (isClosedEnum) {
                addStatement("val·%N·=·%N.readEnum()", enumNumberVar, wrapperParamName)
                addStatement(
                    "val·%N·=·%T.%N(%N)",
                    enumVar,
                    type.resolve(),
                    Const.Enum.GET_ENUM_FOR_OR_NULL_FUNCTION_NAME,
                    enumNumberVar
                )

                add("if·(%N·!=·null)·%N·$assignMode·", enumVar, variableName)
                constructType { add("%N", enumVar) }
                add("\n")

                addStatement(
                    "else·%N·+=·%T(%L, %N.toLong())",
                    Const.Message.Companion.WrapperDeserializationFunction.UNKNOWN_FIELDS_LOCAL_VARIABLE,
                    unknownFieldVarint,
                    fieldNumber,
                    enumNumberVar
                )
            } else {
                add("%N·$assignMode·", variableName)
                constructType {
                    add(
                        when (val type = type) {
                            is ProtoType.DefType -> when (val decl = type.resolveDeclaration()) {
                                is ProtoEnum -> buildReadScalarFieldOpenEnumTypeCode(type)
                                is ProtoMessage -> buildReadScalarFieldMessageTypeCode(type, decl, fieldNumber)
                            }

                            is ProtoType.NonDeclType -> {
                                buildReadScalarFieldBasicTypeCode(type)
                            }
                        }
                    )
                }
                add("\n")
            }
        }
            .build()
    }

    private fun isClosedEnum(type: ProtoType, file: ProtoFile): Boolean {
        return type is ProtoType.DefType && (type.resolveDeclaration() as? ProtoEnum)?.isOpen(file.languageVersion) == false
    }

    private fun FunSpec.Builder.declareWhenEntryForField(field: ProtoMessageField, isPacked: Boolean) {
        addCode(buildFieldTagCode(field, isPacked))

        addCode("·->·")

        when (field.cardinality) {
            is ProtoFieldCardinality.Singular -> {
                beginControlFlow("")
                addCode(
                    buildAssignScalarFieldValueCodeBlock(
                        type = field.type,
                        file = field.file,
                        fieldNumber = field.number,
                        variableName = field.attributeName,
                        assignMode = "=",
                        constructType = { it() }
                    )
                )
                endControlFlow()
            }

            ProtoFieldCardinality.Repeated -> {
                beginControlFlow("{")

                when {
                    field.type is ProtoType.DefType && field.type.isMessage -> {
                        val message = field.type.resolveDeclaration() as ProtoMessage

                        addCode("%N·+=·", field.attributeName)
                        addCode(buildReadScalarFieldMessageTypeCode(field.type, message, field.number))
                        addCode("\n")
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
                            addCode(
                                buildAssignScalarFieldValueCodeBlock(
                                    type = field.type,
                                    file = field.file,
                                    fieldNumber = field.number,
                                    variableName = field.attributeName,
                                    assignMode = "+=",
                                    constructType = { it() }
                                )
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
                                buildAssignScalarFieldValueCodeBlock(
                                    type = field.type,
                                    file = field.file,
                                    fieldNumber = field.number,
                                    variableName = field.attributeName,
                                    assignMode = "+=",
                                    constructType = { it() }
                                )
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
                "%M(%N, %L, %N, %N, %T.%N, %T.%N, ",
                readMapEntry,
                wrapperParamName,
                mapField.number,
                mapField.attributeName,
                Const.Message.Companion.WrapperDeserializationFunction.UNKNOWN_FIELDS_LOCAL_VARIABLE,
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
            addCode(buildReadMapFieldDataCode(mapField.keyType, 1))
            addCode(", ")
            addCode(buildReadMapFieldDataCode(mapField.valuesType, 2))
            addCode(")\n")
        }
    }

    private fun writeReturnStatement(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) {
        builder.apply {
            addCode("return ")

            addCode(
                MessageConstructorCallWriter.getConstructorCallCode(
                    message = message,
                    type = MessageConstructorCallWriter.ConstructorType.BUILD_PARTIAL,
                    getFieldParameter = { CodeBlock.of("%N", it.attributeName) },
                    getMapFieldParameter = { CodeBlock.of("%N", it.attributeName) },
                    getOneOfFieldParameter = { CodeBlock.of("%N", it.attributeName) },
                    getUnknownFieldsParameter = {
                        CodeBlock.of(
                            "%N",
                            Const.Message.Companion.WrapperDeserializationFunction.UNKNOWN_FIELDS_LOCAL_VARIABLE
                        )
                    },
                    getExtensionParameter = {
                        CodeBlock.of(
                            "%N.build()",
                            Const.Message.Companion.WrapperDeserializationFunction.EXTENSION_BUILDER_LOCAL_VARIABLE
                        )
                    }
                )
            )
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
     * @return a CodeBlock that reads the data for [type] under the assumption that it is a scalar field
     */
    private fun buildReadScalarFieldBasicTypeCode(type: ProtoType.NonDeclType): CodeBlock {
        return CodeBlock.of(
            "%N.%N()",
            wrapperParamName,
            getReadScalarFunctionName(type)
        )
    }

    private fun buildReadScalarFieldMessageTypeCode(
        type: ProtoType.DefType,
        message: ProtoMessage,
        fieldNumber: Int
    ): CodeBlock {
        return CodeBlock.builder()
            .add(
                "%N.%N(%T.Companion, ",
                wrapperParamName,
                getReadScalarFunctionName(type),
                type.resolve()
            )
            .add(buildExtensionRegistryCodeForMessage(message))
            .apply {
                if (message.type == ProtoMessage.Type.GROUP) {
                    add(", %L", fieldNumber)
                }
            }
            .add(")")
            .build()
    }

    private fun buildReadScalarFieldOpenEnumTypeCode(type: ProtoType.DefType): CodeBlock {
        return CodeBlock.of(
            "%T.%N(%N.readEnum())",
            type.resolve(),
            Const.Enum.GET_ENUM_FOR_FUNCTION_NAME,
            wrapperParamName
        )
    }

    private fun buildReadMapFieldDataCode(type: ProtoType, fieldNumber: Int): CodeBlock {
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
                            .add("{·")
                            .add(buildReadScalarFieldMessageTypeCode(type, decl, fieldNumber))
                            .add("}")
                            .build()
                    }

                    is ProtoEnum -> {
                        CodeBlock.of(
                            "{·%T.%N(readEnum())·}",
                            type.resolve(),
                            if (decl.isOpen(type.file.languageVersion)) Const.Enum.GET_ENUM_FOR_FUNCTION_NAME
                            else Const.Enum.GET_ENUM_FOR_OR_NULL_FUNCTION_NAME
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
                when (val decl = protoType.resolveDeclaration()) {
                    is ProtoEnum -> "readEnum"
                    is ProtoMessage -> when (decl.type) {
                        ProtoMessage.Type.DEFAULT -> "readMessage"
                        ProtoMessage.Type.GROUP -> "readGroup"
                    }
                }
            }
        }
    }

    private fun buildExtensionRegistryCodeForMessage(message: ProtoMessage): CodeBlock {
        return if (message.isExtendable) {
            // bug in KotlinPoet: Member declaration resolves incorrectly, so we use %T.%N
            CodeBlock.of(
                "%T.%N",
                message.className.nestedClass("Companion"),
                Const.Message.Companion.defaultExtensionRegistryProperty.name
            )
        } else {
            CodeBlock.of("%T.empty()", kmExtensionRegistry)
        }
    }
}
