package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatMakeTag

/**
 * Writes the serialize function in the actual source code.
 */
class SerializationFunctionExtension : BaseSerializationExtension() {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addFunction(
            FunSpec
                .builder(Const.Message.SerializeFunction.NAME)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    Const.Message.SerializeFunction.STREAM_PARAM,
                    CodedOutputStream
                )
                .apply {
                    if (sourceTarget is SourceTarget.Actual) {
                        addModifiers(KModifier.ACTUAL)
                        buildSerializeFunction(this, message)
                    }
                }
                .build()
        )
    }

    /**
     * Generate the serialize function, adds a write call for each attribute
     */
    private fun buildSerializeFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) {
        builder.apply {
            message.fields.forEach { field ->
                when {
                    field.hasIsSetProperty -> {
                        addCode(
                            getWriteScalarFieldCode(
                                field = field,
                                streamParam = Const.Message.SerializeFunction.STREAM_PARAM,
                                performIsFieldSetCheck = true
                            )
                        )
                    }

                    field.cardinality.isImplicit -> {
                        addCode("if·(")
                        addCode(field.type.isNotDefaultValueCode(field.attributeName, false))
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

                        if (field.isPacked) {
                            //Write packed.
                            // From GPBDescriptor.m: GPBWireFormatForType(description->dataType,
                            //                                  ((description->flags & GPBFieldPacked) != 0))
                            addStatement(
                                "%N.%N(%L, %N, %Lu)",
                                Const.Message.SerializeFunction.STREAM_PARAM,
                                writeArrayFunction,
                                field.number,
                                field.attributeName,
                                wireFormatMakeTag(field.number, field.type.wireType, true)
                            )
                        } else {
                            val code = if (field.type.isPackable) {
                                "%N.%N(%L, %N, 0u)"
                            } else {
                                "%N.%N(%L, %N)"
                            }

                            addStatement(
                                code,
                                Const.Message.SerializeFunction.STREAM_PARAM,
                                writeArrayFunction,
                                field.number,
                                field.attributeName
                            )
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
                    oneOf.attributeName,
                    Const.Message.OneOf.SERIALIZE_FUNCTION_NAME,
                    Const.Message.SerializeFunction.STREAM_PARAM
                )
            }

            addStatement(
                "%N.%N(%N)",
                Const.Message.SerializeFunction.STREAM_PARAM,
                "writeUnknownFields",
                Const.Message.Constructor.UnknownFields.name
            )

            if (message.isExtendable) {
                addStatement(
                    "%N.%N(%N)",
                    Const.Message.SerializeFunction.STREAM_PARAM,
                    "writeMessageExtensions",
                    Const.Message.Constructor.MessageExtensions.name
                )
            }
        }
    }

    fun buildMapAttributeSerializeCode(
        builder: FunSpec.Builder,
        field: ProtoMapField
    ) {
        builder.apply {
            addCode(
                "%N.%N(%L, %N, ",
                Const.Message.SerializeFunction.STREAM_PARAM,
                "writeMap",
                field.number,
                field.attributeName
            )

            addCode(getComputeMapValueRequiredSizeCode(field.keyType))

            addCode(", ")

            addCode(getComputeMapValueRequiredSizeCode(field.valuesType))

            addCode(", ")

            addMapKeyTypeSerializationCode(field.keyType)
            addCode(", ")
            addMapValueTypeSerializationCode(field.valuesType)
            addCode(")\n")
        }
    }

    /**
     * Append code that serializes the given types in a function call for type CodedOutputStream.(fieldNumber: Int, K) -> Unit
     */
    private fun FunSpec.Builder.addMapKeyTypeSerializationCode(type: ProtoType.MapKeyType) {
        addWriteScalarFieldCode(type)
    }

    /**
     * Append code that serializes the given types in a function call for type CodedOutputStream.(fieldNumber: Int, V) -> Unit
     */
    private fun FunSpec.Builder.addMapValueTypeSerializationCode(type: ProtoType) {
        when (type) {
            is ProtoType.NonDeclType -> {
                addWriteScalarFieldCode(type)
            }

            is ProtoType.DefType -> {
                when (type.declType) {
                    ProtoType.DefType.DeclarationType.MESSAGE -> {
                        addCode(
                            "{·fieldNumber,·msg·-> writeMessage(fieldNumber, msg)·}"
                        )
                    }

                    ProtoType.DefType.DeclarationType.ENUM -> {
                        addWriteEnumFieldCode()
                    }
                }
            }
        }
    }

    private fun FunSpec.Builder.addWriteScalarFieldCode(type: ProtoType.NonDeclType) {
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

    companion object {
        fun getWriteScalarFieldCode(
            field: ProtoRegularField,
            streamParam: String,
            performIsFieldSetCheck: Boolean
        ): CodeBlock {
            val serializationCodeBlock = when (val type = field.type) {
                is ProtoType.NonDeclType -> {
                    val functionName = getWriteScalarFunctionName(type)

                    CodeBlock.of(
                        "%N.%N(%L, %N)\n",
                        streamParam,
                        functionName,
                        field.number,
                        field.attributeName
                    )
                }

                is ProtoType.DefType -> {
                    when (type.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> {
                            CodeBlock.of(
                                "%N.writeMessage(%L, %N)",
                                streamParam,
                                field.number,
                                field.attributeName
                            )
                        }

                        ProtoType.DefType.DeclarationType.ENUM -> {
                            CodeBlock.of(
                                "%N.writeEnum(%L, %N.%N)\n",
                                streamParam,
                                field.number,
                                field.attributeName,
                                Const.Enum.NUMBER_PROPERTY_NAME
                            )
                        }
                    }
                }
            }

            return if (performIsFieldSetCheck && field is ProtoMessageField) {
                CodeBlock.builder()
                    .beginControlFlow(
                        "if (%N)",
                        field.isSetProperty.attributeName
                    )
                    .add(serializationCodeBlock)
                    .endControlFlow()
                    .build()
            } else {
                serializationCodeBlock
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
    }
}
