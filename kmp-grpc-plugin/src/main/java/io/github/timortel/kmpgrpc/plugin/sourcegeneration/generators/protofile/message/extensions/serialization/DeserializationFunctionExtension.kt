package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoRegularField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

/**
 * Adds the deserialization function using CodedOutputStream to the companion object.
 */
class DeserializationFunctionExtension : BaseSerializationExtension() {

    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addFunction(
            //The function that builds the message from a stream.
            FunSpec
                .builder(Const.Message.Companion.WrapperDeserializationFunction.NAME)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM,
                    CodedInputStream
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
        val wrapperParamName =
            Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM

        val unknownFieldsFieldName = "unknownFields"
        builder.addStatement(
            "val %N: %T = mutableListOf()",
            unknownFieldsFieldName,
            MUTABLE_LIST.parameterizedBy(unknownField)
        )

        val addVariable = { fieldName: String, type: TypeName, isMutable: Boolean, defaultValue: CodeBlock ->
            builder.addCode(
                if (isMutable) "var %N: %T = " else "val %N: %T = ",
                fieldName,
                type
            )

            builder.addCode(defaultValue)
            builder.addCode("\n")
        }

        builder.apply {
            message.fields.forEach { field ->
                when (field.cardinality) {
                    is ProtoFieldCardinality.Singular -> {
                        val type = if (field.needsIsSetProperty)
                            field.type.resolve().copy(nullable = true)
                        else field.type.resolve()

                        val defaultValue = if (field.needsIsSetProperty) {
                            CodeBlock.of("null")
                        } else field.type.defaultValue()

                        addVariable(
                            field.attributeName,
                            type,
                            true,
                            defaultValue
                        )
                    }

                    ProtoFieldCardinality.Repeated -> {
                        addVariable(
                            field.attributeName,
                            MUTABLE_LIST.parameterizedBy(field.type.resolve()),
                            false,
                            CodeBlock.of("mutableListOf()")
                        )
                    }
                }
            }

            message.mapFields.forEach { field ->
                addVariable(
                    field.attributeName,
                    MUTABLE_MAP.parameterizedBy(field.keyType.resolve(), field.valuesType.resolve()),
                    false,
                    CodeBlock.of("mutableMapOf()")
                )
            }

            message.oneOfs.forEach { oneOf ->
                addVariable(
                    oneOf.attributeName,
                    oneOf.sealedClassName,
                    true,
                    CodeBlock.of("%T", oneOf.sealedClassNameNotSet)
                )
            }

            beginControlFlow("while (true)")
            addStatement(
                "val tag = %N.readTag()",
                wrapperParamName
            )
            addStatement("if (tag == 0) break")

            beginControlFlow("when (tag)")

            val getScalarAndRepeatedTagCode = { type: ProtoType, isRepeated: Boolean, fieldNumber: Int ->
                val isPacked = type.isPackable && isRepeated

                CodeBlock.of(
                    "%M(%L, %M(%T.%N, %L))",
                    WireFormatMakeTag,
                    fieldNumber,
                    WireFormatForType,
                    DataType,
                    type.wireType,
                    isPacked
                )
            }

            val getScalarReadFieldCode = { field: ProtoRegularField ->
                when (val type = field.type) {
                    is ProtoType.NonDeclType -> {
                        CodeBlock.of(
                            "%N.%N()",
                            wrapperParamName,
                            getReadScalarFunctionName(field.type)
                        )
                    }

                    is ProtoType.DefType -> {
                        when (type.declType) {
                            ProtoType.DefType.DeclarationType.MESSAGE -> {
                                CodeBlock.of(
                                    "%N.%N(%T.Companion)",
                                    wrapperParamName,
                                    "readMessage",
                                    field.type.resolve()
                                )
                            }

                            ProtoType.DefType.DeclarationType.ENUM -> {
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

            message.mapFields.forEach { mapField ->
                addCode(
                    "%M(%L, %M(%T.%N, false)).toInt()",
                    WireFormatMakeTag,
                    mapField.number,
                    WireFormatForType,
                    DataType,
                    "MESSAGE"
                )

                addCode(" -> ")

                addCode(
                    "%N.%N(%N, %T.%N, %T.%N, ",
                    Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM,
                    "readMapEntry",
                    mapField.attributeName,
                    DataType,
                    mapField.keyType.wireType,
                    DataType,
                    mapField.valuesType.wireType,
                )

                val getDefaultEntry = { type: ProtoType ->
                    when (type) {
                        is ProtoType.DefType -> type.defaultValue(ProtoType.MessageDefaultValue.EMPTY)
                        is ProtoType.NonDeclType -> type.defaultValue()
                    }
                }

                val addReadScalarCode = { type: ProtoType ->
                    when (type) {
                        is ProtoType.NonDeclType -> {
                            addCode(
                                "{·%N()·}",
                                getReadScalarFunctionName(type)
                            )
                        }

                        is ProtoType.DefType -> {
                            when (type.declType) {
                                ProtoType.DefType.DeclarationType.MESSAGE -> {
                                    addCode(
                                        "{·%N(%T.Companion)}",
                                        "readMessage",
                                        type.resolve()
                                    )
                                }

                                ProtoType.DefType.DeclarationType.ENUM -> {
                                    addCode(
                                        "{·%T.%N(readEnum())·}",
                                        type.resolve(),
                                        Const.Enum.GET_ENUM_FOR_FUNCTION_NAME
                                    )
                                }
                            }
                        }
                    }
                }

                //Write default values
                addCode(getDefaultEntry(mapField.keyType))
                addCode(", ")
                addCode(getDefaultEntry(mapField.valuesType))
                addCode(", ")

                addReadScalarCode(mapField.keyType)

                addCode(", ")
                addReadScalarCode(mapField.valuesType)
                addCode(")\n")
            }

            message.fields.forEach { field ->
                addCode(
                    getScalarAndRepeatedTagCode(
                        field.type,
                        field.cardinality == ProtoFieldCardinality.Repeated,
                        field.number
                    )
                )

                addCode(" -> ")

                when (field.cardinality) {
                    is ProtoFieldCardinality.Singular -> {
                        addCode("%N·=·", field.attributeName)
                        addCode(getScalarReadFieldCode(field))
                        addCode("\n")
                    }

                    ProtoFieldCardinality.Repeated -> {
                        beginControlFlow("{")

                        when {
                            field.type == ProtoType.StringType -> {
                                addCode(
                                    "%N·+=·%N.readString()",
                                    field.attributeName,
                                    wrapperParamName
                                )
                            }

                            field.type == ProtoType.BytesType -> {
                                addCode(
                                    "%N·+=·%N.readBytes()",
                                    field.attributeName,
                                    wrapperParamName
                                )
                            }

                            field.type.isMessage -> {
                                addCode(
                                    "%N·+=·%N.%N(%T.Companion)\n",
                                    field.attributeName,
                                    wrapperParamName,
                                    "readMessage",
                                    field.type.resolve()
                                )
                            }

                            else -> {
                                //All packable
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
                        }

                        endControlFlow()
                    }
                }
            }

            message.oneOfs.forEach { oneOf ->
                oneOf.fields.forEach { field ->
                    addCode(getScalarAndRepeatedTagCode(field.type, false, field.number))
                    addCode(
                        "·->·%N·=·%T(",
                        oneOf.attributeName,
                        field.sealedClassChildName
                    )
                    addCode(getScalarReadFieldCode(field))
                    addCode(")\n")
                }
            }

            // Unknown field
            addStatement("else -> %N.%N(tag)?.let { unknownFields.add(it) }", wrapperParamName, "readUnknownField")
            // Unknown field

            endControlFlow()

            endControlFlow()

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
                CodeBlock.of("%N·=·%N", Const.Message.Constructor.UnknownFields.name, unknownFieldsFieldName)

            addCode(listOf(fieldsBlock, unknownFieldsBlock).joinCodeBlocks(separator))

            addCode(")\n")
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
}
