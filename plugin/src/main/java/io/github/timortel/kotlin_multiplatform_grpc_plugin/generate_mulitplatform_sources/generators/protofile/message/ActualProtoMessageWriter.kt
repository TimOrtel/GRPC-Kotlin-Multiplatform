package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.message

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.ActualProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoRegularField

abstract class ActualProtoMessageWriter : ProtoMessageWriter(true) {

    override val protoFieldWriter: ProtoFieldWriter = ActualProtoFieldWriter
    override val protoEnumerationWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter

    abstract val serializeFunctionCode: CodeBlock
    abstract val serializedDataType: TypeName

    abstract val deserializeFunctionCode: CodeBlock

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .apply {
                        //one of attributes do not get a parameter, as they get the one of parameter
                        message.fields.forEach { field ->
                            when (field.cardinality) {
                                is ProtoFieldCardinality.Singular -> {
                                    val type = if (field.type.isMessage) field.type.resolve().copy(nullable = true)
                                    else field.type.resolve()

                                    addParameter(
                                        ParameterSpec
                                            .builder(field.fieldName, type)
                                            .defaultValue(field.type.defaultValue())
                                            .build()
                                    )
                                }

                                ProtoFieldCardinality.Repeated -> {
                                    addParameter(
                                        ParameterSpec
                                            .builder(field.fieldName, LIST.parameterizedBy(field.type.resolve()))
                                            .defaultValue("emptyList()")
                                            .build()
                                    )
                                }
                            }
                        }

                        message.mapFields.forEach { mapField ->
                            addParameter(
                                ParameterSpec
                                    .builder(
                                        mapField.fieldName,
                                        MAP.parameterizedBy(
                                            mapField.keyType.resolve(),
                                            mapField.valuesType.resolve(),
                                        )
                                    )
                                    .defaultValue("emptyMap()")
                                    .build()
                            )
                        }

                        message.oneOfs.forEach { oneOf ->
                            addParameter(
                                ParameterSpec
                                    .builder(
                                        oneOf.fieldName,
                                        oneOf.sealedClassName
                                    )
                                    .defaultValue("%T", oneOf.sealedClassNameNotSet)
                                    .build()
                            )
                        }
                    }
                    .build()
            )

            addSuperinterface(kmMessage)

            addFunction(
                FunSpec
                    .builder(Const.Message.SerializeFunction.NAME)
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        Const.Message.SerializeFunction.STREAM_PARAM,
                        CodedOutputStream
                    )
                    .apply { buildSerializeFunction(this, message) }
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("serialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(serializedDataType)
                    .addCode(
                        serializeFunctionCode
                    )

                    .build()
            )

            addType(
                TypeSpec
                    .companionObjectBuilder()
                    .addSuperinterface(
                        MessageDeserializer.parameterizedBy(
                            message.className,
                            serializedDataType
                        )
                    )
                    .addFunction(
                        // The function that builds the message from NSData
                        FunSpec
                            .builder(Const.Message.Companion.DataDeserializationFunction.NAME)
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter(
                                Const.Message.Companion.DataDeserializationFunction.DATA_PARAM,
                                serializedDataType
                            )
                            .addCode(
                                deserializeFunctionCode
                            )
                            .returns(message.className)
                            .build()
                    )
                    .addFunction(
                        //The function that builds the message from a stream.
                        FunSpec
                            .builder(Const.Message.Companion.WrapperDeserializationFunction.NAME)
                            .addParameter(
                                Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM,
                                CodedInputStream
                            )
                            .returns(message.className)
                            .apply { buildWrapperDeserializationFunction(this, message) }
                            .build()
                    )
                    .build()
            )
        }
    }

    /**
     * Generate the serialize function, adds a write call for each attribute
     */
    private fun buildSerializeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            message.fields.forEach { field ->
                when {
                    field.cardinality == ProtoFieldCardinality.Optional || (field.type.isMessage && field.cardinality != ProtoFieldCardinality.Repeated) -> {
                        addCode(
                            getWriteScalarFieldCode(
                                field = field,
                                streamParam = Const.Message.SerializeFunction.STREAM_PARAM,
                                performIsFieldSetCheck = true
                            )
                        )
                    }

                    field.cardinality == ProtoFieldCardinality.Implicit -> {
                        addCode("if·(%N·!=·", field.fieldName)
                        addCode(field.type.defaultValue())
                        beginControlFlow(")·{ ")
                        addCode(
                            getWriteScalarFieldCode(
                                field = field,
                                streamParam = Const.Message.SerializeFunction.STREAM_PARAM,
                                performIsFieldSetCheck = false
                            )
                        )
                        endControlFlow()
                    }

                    field.cardinality == ProtoFieldCardinality.Repeated -> {
                        val writeArrayFunction = getWriteArrayFunctionName(field.type)

                        when {
                            field.type.isPackable -> {
                                //Write packed.
                                // From GPBDescriptor.m: GPBWireFormatForType(description->dataType,
                                //                                  ((description->flags & GPBFieldPacked) != 0))
                                addStatement(
                                    "%N.%N(%L, %N, %M(%L, %M(%T.%N, true)).toUInt())",
                                    Const.Message.SerializeFunction.STREAM_PARAM,
                                    writeArrayFunction,
                                    field.number,
                                    field.fieldName,
                                    WireFormatMakeTag,
                                    field.number,
                                    WireFormatForType,
                                    DataType,
                                    getDataTypeForProtoType(field.type)
                                )
                            }

                            else -> {
                                addStatement(
                                    "%N.%N(%L, %N)",
                                    Const.Message.SerializeFunction.STREAM_PARAM,
                                    writeArrayFunction,
                                    field.number,
                                    field.fieldName
                                )
                            }
                        }
                    }
                }
            }

            message.mapFields.forEach { mapField ->
                buildMapAttributeSerializeCode(builder, mapField)
            }

            message.oneOfs.forEach { oneOf ->
                addStatement(
                    "%N.%N(%N)",
                    oneOf.fieldName,
                    Const.Message.OneOf.SERIALIZE_FUNCTION_NAME,
                    Const.Message.SerializeFunction.STREAM_PARAM
                )
            }
        }
    }

    /**
     * Called by [buildSerializeFunction] when an attribute with the map type needs to be serialized
     */
    protected abstract fun buildMapAttributeSerializeCode(
        builder: FunSpec.Builder,
        field: ProtoMapField
    )

    /**
     * Append code that serializes the given types in a function call for type CodedOutputStream.(fieldNumber: Int, K) -> Unit
     */
    protected fun FunSpec.Builder.addMapKeyTypeSerializationCode(type: ProtoType.MapKeyType) {
        addWriteScalarFieldCode(type)
    }

    /**
     * Append code that serializes the given types in a function call for type CodedOutputStream.(fieldNumber: Int, V) -> Unit
     */
    protected fun FunSpec.Builder.addMapValueTypeSerializationCode(type: ProtoType) {
        when (type) {
            is ProtoType.NonDeclType -> {
                addWriteScalarFieldCode(type)
            }

            is ProtoType.DefType -> {
                when (type.declType) {
                    ProtoType.DefType.DeclarationType.MESSAGE -> {
                        addCode(
                            "{·fieldNumber,·msg·-> %N.writeMessage(fieldNumber, msg)·}",
                            Const.Message.SerializeFunction.STREAM_PARAM
                        )
                    }

                    ProtoType.DefType.DeclarationType.ENUM -> {
                        addWriteEnumFieldCode()
                    }
                }
            }
        }
    }

    fun FunSpec.Builder.addWriteScalarFieldCode(type: ProtoType.NonDeclType) {
        addCode(
            "%T::%N",
            CodedOutputStream,
            getWriteScalarFunctionName(type)
        )
    }

    private fun FunSpec.Builder.addWriteEnumFieldCode() {
        addCode(
            "{·fieldNumber,·it·-> writeEnum(fieldNumber, it.%N)·}",
            Const.Enum.NUMBER_PROPERTY_NAME
        )
    }


    private fun buildWrapperDeserializationFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) {
        val wrapperParamName =
            Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM

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
                        val type = if (field.type.isMessage)
                            field.type.resolve().copy(nullable = true)
                        else field.type.resolve()

                        addVariable(
                            field.fieldName,
                            type,
                            true,
                            field.type.defaultValue()
                        )
                    }

                    ProtoFieldCardinality.Repeated -> {
                        addVariable(
                            field.fieldName,
                            MUTABLE_LIST.parameterizedBy(field.type.resolve()),
                            false,
                            CodeBlock.of("mutableListOf()"))
                    }
                }
            }

            message.mapFields.forEach { field ->
                addVariable(
                    field.fieldName,
                    MUTABLE_MAP.parameterizedBy(field.keyType.resolve(), field.valuesType.resolve()),
                    false,
                    CodeBlock.of("mutableMapOf()")
                )
            }

            message.oneOfs.forEach { oneOf ->
                addVariable(
                    oneOf.fieldName,
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

                val dataType = getDataTypeForProtoType(type)

                CodeBlock.of(
                    "%M(%L, %M(%T.%N, %L)).toInt()",
                    WireFormatMakeTag,
                    fieldNumber,
                    WireFormatForType,
                    DataType,
                    dataType,
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
                                    "%M(%N, %T.Companion::%N)",
                                    readKMMessage,
                                    wrapperParamName,
                                    field.type.resolve(),
                                    Const.Message.Companion.WrapperDeserializationFunction.NAME
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
                    "%M(%N, %N, %T.%N, %T.%N, ",
                    readMapEntry,
                    Const.Message.Companion.WrapperDeserializationFunction.STREAM_PARAM,
                    mapField.fieldName,
                    DataType,
                    getDataTypeForProtoType(mapField.keyType),
                    DataType,
                    getDataTypeForProtoType(mapField.valuesType),
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
                                        "{·%M(this, %T.Companion::%N)}",
                                        readKMMessage,
                                        type.resolve(),
                                        Const.Message.Companion.WrapperDeserializationFunction.NAME
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
                        addCode("%N·=·", field.fieldName)
                        addCode(getScalarReadFieldCode(field))
                        addCode("\n")
                    }

                    ProtoFieldCardinality.Repeated -> {
                        beginControlFlow("{")

                        when {
                            field.type == ProtoType.StringType -> {
                                addCode(
                                    "%N·+=·%N.readString()",
                                    field.fieldName,
                                    wrapperParamName
                                )
                            }

                            field.type.isMessage -> {
                                addCode(
                                    "%N·+=·%M(%N, %T.Companion::%N)\n",
                                    field.fieldName,
                                    readKMMessage,
                                    wrapperParamName,
                                    field.type.resolve(),
                                    Const.Message.Companion.WrapperDeserializationFunction.NAME
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
                                        field.fieldName,
                                        field.type.resolve(),
                                        Const.Enum.GET_ENUM_FOR_FUNCTION_NAME,
                                        wrapperParamName
                                    )
                                } else {
                                    val functionName = getReadScalarFunctionName(field.type)

                                    addStatement(
                                        "%N·+=·%N.%N()",
                                        field.fieldName,
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
                        oneOf.fieldName,
                        field.sealedClassChildName
                    )
                    addCode(getScalarReadFieldCode(field))
                    addCode(")\n")
                }
            }

            endControlFlow()

            endControlFlow()

            addCode("return %T(", message.className)
            addCode(
                (message.fields + message.mapFields + message.oneOfs)
                    .joinToCodeBlock(separator = ", ") { field ->
                        add(
                            "%N·=·%N",
                            field.fieldName,
                            field.fieldName
                        )
                    }
            )

            addCode(")\n")
        }
    }

    companion object {
        fun getWriteScalarFieldCode(
            field: ProtoRegularField,
            streamParam: String,
            performIsFieldSetCheck: Boolean
        ): CodeBlock {
            return when (val type = field.type) {
                is ProtoType.NonDeclType -> {
                    val functionName = getWriteScalarFunctionName(type)

                    CodeBlock.of(
                        "%N.%N(%L, %N)\n",
                        streamParam,
                        functionName,
                        field.number,
                        field.fieldName
                    )
                }

                is ProtoType.DefType -> {
                    when (type.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> {
                            CodeBlock.builder().apply {
                                if (performIsFieldSetCheck) {
                                    beginControlFlow(
                                        "if (%N)",
                                        field.isSetPropertyName
                                    )
                                }

                                addStatement(
                                    "%N.writeMessage(%L, %N)",
                                    streamParam,
                                    field.number,
                                    field.fieldName
                                )

                                if (performIsFieldSetCheck) endControlFlow()
                            }.build()
                        }

                        ProtoType.DefType.DeclarationType.ENUM -> {
                            CodeBlock.of(
                                "%N.writeEnum(%L, %N.%N)\n",
                                streamParam,
                                field.number,
                                field.fieldName,
                                Const.Enum.NUMBER_PROPERTY_NAME
                            )
                        }
                    }
                }
            }
        }

        private fun getWriteScalarFunctionName(protoType: ProtoType): String {
            return when (protoType) {
                ProtoType.DoubleType -> "writeDouble"
                ProtoType.FloatType -> "writeFloat"
                ProtoType.Int32Type -> "writeInt32"
                ProtoType.Int64Type -> "writeInt64"
                ProtoType.UInt32Type -> "writeUInt32"
                ProtoType.UInt64Type -> "writeUInt64"
                ProtoType.SInt32Type -> "writeSInt32"
                ProtoType.SInt64Type -> "writeSInt64"
                ProtoType.Fixed32Type -> "writeFixed32"
                ProtoType.Fixed64Type -> "writeFixed64"
                ProtoType.SFixed32Type -> "writeSFixed32"
                ProtoType.SFixed64Type -> "writeSFixed64"
                ProtoType.BoolType -> "writeBool"
                ProtoType.StringType -> "writeString"
                ProtoType.BytesType -> "writeBytes"
                is ProtoType.DefType -> {
                    when (protoType.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> "writeMessage"
                        ProtoType.DefType.DeclarationType.ENUM -> "writeEnum"
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

        private fun getWriteArrayFunctionName(protoType: ProtoType): String {
            return when (protoType) {
                ProtoType.DoubleType -> "writeDoubleArray"
                ProtoType.FloatType -> "writeFloatArray"
                ProtoType.Int32Type -> "writeInt32Array"
                ProtoType.Int64Type -> "writeInt64Array"
                ProtoType.UInt32Type -> "writeUInt32Array"
                ProtoType.UInt64Type -> "writeUInt64Array"
                ProtoType.SInt32Type -> "writeSInt32Array"
                ProtoType.SInt64Type -> "writeSInt64Array"
                ProtoType.Fixed32Type -> "writeFixed32Array"
                ProtoType.Fixed64Type -> "writeFixed64Array"
                ProtoType.SFixed32Type -> "writeSFixed32Array"
                ProtoType.SFixed64Type -> "writeSFixed64Array"
                ProtoType.BoolType -> "writeBoolArray"
                ProtoType.StringType -> "writeStringArray"
                ProtoType.BytesType -> "writeBytesArray"
                is ProtoType.DefType -> {
                    when (protoType.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> "writeMessageArray"
                        ProtoType.DefType.DeclarationType.ENUM -> "writeEnumArray"
                    }
                }
            }
        }

        private fun getDataTypeForProtoType(protoType: ProtoType) =
            when (protoType) {
                ProtoType.BoolType -> "BOOL"
                ProtoType.Fixed32Type -> "FIXED32"
                ProtoType.Fixed64Type -> "FIXED64"
                ProtoType.Int32Type -> "INT32"
                ProtoType.Int64Type -> "INT64"
                ProtoType.SFixed32Type -> "SFIXED32"
                ProtoType.SFixed64Type -> "SFIXED64"
                ProtoType.SInt32Type -> "SINT32"
                ProtoType.SInt64Type -> "SINT64"
                ProtoType.StringType -> "STRING"
                ProtoType.UInt32Type -> "UINT32"
                ProtoType.UInt64Type -> "UINT64"
                ProtoType.BytesType -> "BYTES"
                ProtoType.DoubleType -> "DOUBLE"
                ProtoType.FloatType -> "FLOAT"
                is ProtoType.DefType -> {
                    when (protoType.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> "MESSAGE"
                        ProtoType.DefType.DeclarationType.ENUM -> "ENUM"
                    }
                }
            }
    }
}