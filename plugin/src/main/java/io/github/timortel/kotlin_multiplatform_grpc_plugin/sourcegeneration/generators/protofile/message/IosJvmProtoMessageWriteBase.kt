package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.oneof.JvmIosProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoRegularField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.joinToCodeBlock

abstract class IosJvmProtoMessageWriteBase : ActualProtoMessageWriter() {

    override val protoOneOfWriter: ProtoOneOfWriter = JvmIosProtoOneOfWriter

    override fun applyToClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage
    ) {
        super.applyToClass(builder, message)

        builder.apply {
            addProperty(
                PropertySpec
                    .builder("requiredSize", INT, KModifier.OVERRIDE)
                    .initializer(buildRequiredSizeInitializer(message))
                    .build()
            )
        }
    }

    private fun buildRequiredSizeInitializer(message: ProtoMessage): CodeBlock {
        return CodeBlock
            .builder()
            .apply {
                val separator = "·+\n"

                val fieldsCodeBlock = message.fields.joinToCodeBlock(separator) { field ->
                    when {
                        field.cardinality == ProtoFieldCardinality.Optional || (field.type.isMessage && field.cardinality != ProtoFieldCardinality.Repeated) -> {
                            add("if·(%N)·{·", field.isSetPropertyName)
                            add(getCodeForRequiredSizeForScalarAttributeC(field))
                            add("}·else·{·0·}")
                        }

                        field.cardinality == ProtoFieldCardinality.Implicit -> {
                            add("if·(%N·==·", field.attributeName)
                            add(field.type.defaultValue())
                            add(")·0·else·{ ")
                            add(getCodeForRequiredSizeForScalarAttributeC(field))
                            add(" }")
                        }

                        field.cardinality == ProtoFieldCardinality.Repeated -> {
                            add(
                                "if (%N.isEmpty())·0·else·{\n%N.let·{·e·->\n",
                                field.attributeName,
                                field.attributeName
                            )
                            beginControlFlow("val·dataSize·=·e.sumOf·{")
                            when (field.type) {
                                is ProtoType.NonDeclType -> {
                                    add(
                                        "%M(it)",
                                        getComputeDataTypeSizeMember(field.type, false)
                                    )
                                }

                                is ProtoType.DefType -> {
                                    when (field.type.declType) {
                                        ProtoType.DefType.DeclarationType.MESSAGE -> {
                                            add(
                                                "%M(it)",
                                                MemberName(
                                                    PACKAGE_IO,
                                                    "computeMessageSizeNoTag"
                                                )
                                            )
                                        }

                                        ProtoType.DefType.DeclarationType.ENUM -> {
                                            add(
                                                "%M(it.%N)",
                                                MemberName(PACKAGE_IO, "computeEnumSizeNoTag"),
                                                Const.Enum.NUMBER_PROPERTY_NAME
                                            )

                                        }
                                    }
                                }
                            }
                            endControlFlow()
                            addStatement("val tagSize = %M(%L)", ComputeTagSize, field.number)

                            if (field.type.isPackable) {
                                addStatement(
                                    "dataSize + tagSize + %M(tagSize)",
                                    ComputeInt32SizeNoTag
                                )
                            } else {
                                addStatement("dataSize + %N.size * tagSize", field.attributeName)
                            }

                            add("\n}")
                            add("\n}")
                        }
                    }
                }

                val mapFieldsCodeBlock = message.mapFields.joinToCodeBlock(separator) { mapField ->
                    add(
                        "%M(%L, %N, ::%M, ",
                        computeMapSize,
                        mapField.number,
                        mapField.attributeName,
                        getComputeDataTypeSizeMember(mapField.keyType, true)
                    )

                    add(getComputeMapValueRequiredSizeCode(mapField.valuesType))

                    add(")")
                }

                val oneOfsBlock = message.oneOfs.joinToCodeBlock(separator) { oneOf ->
                    add(
                        "%N.%N",
                        oneOf.attributeName,
                        Const.Message.OneOf.REQUIRED_SIZE_PROPERTY_NAME
                    )
                }

                add(
                    listOf(fieldsCodeBlock, mapFieldsCodeBlock, oneOfsBlock)
                        .filter { it.isNotEmpty() }
                        .joinToCodeBlock(separator) { add(it) }
                )

                // Fallback for messages without any fields.
                if (message.isEmpty) {
                    add("0")
                }
            }
            .build()
    }

    override fun buildMapAttributeSerializeCode(builder: FunSpec.Builder, field: ProtoMapField) {
        builder.apply {
            addCode(
                "%M(%N, %L, %N, ",
                writeMap,
                Const.Message.SerializeFunction.STREAM_PARAM,
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

    private fun getComputeMapValueRequiredSizeCode(type: ProtoType): CodeBlock {
        return when (type) {
            is ProtoType.DefType -> {
                when (type.declType) {
                    ProtoType.DefType.DeclarationType.MESSAGE -> {
                        CodeBlock.of(
                            "::%M",
                            getComputeDataTypeSizeMember(type, true)
                        )
                    }

                    ProtoType.DefType.DeclarationType.ENUM -> {
                        CodeBlock.of(
                            "{·fieldNumber,·it·-> %M(fieldNumber, it.%N)·}",
                            getComputeDataTypeSizeMember(type, true),
                            Const.Enum.NUMBER_PROPERTY_NAME
                        )
                    }
                }
            }

            is ProtoType.NonDeclType -> {
                CodeBlock.of(
                    "::%M",
                    getComputeDataTypeSizeMember(type, true)
                )
            }
        }
    }

    companion object {
        /**
         * @return the function that compute the size of the data type.
         */
        private fun getComputeDataTypeSizeMember(
            protoType: ProtoType,
            withTag: Boolean
        ): MemberName {
            return when (protoType) {
                is ProtoType.DefType -> {
                    when (protoType.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> computeMessageSize
                        ProtoType.DefType.DeclarationType.ENUM -> {
                            MemberName(PACKAGE_IO, "computeEnumSize${if (!withTag) "NoTag" else ""}")
                        }
                    }
                }

                is ProtoType.NonDeclType -> {
                    val name = when (protoType) {
                        ProtoType.BytesType -> "Bytes"
                        ProtoType.DoubleType -> "Double"
                        ProtoType.FloatType -> "Float"
                        ProtoType.BoolType -> "Bool"
                        ProtoType.Fixed32Type -> "Fixed32"
                        ProtoType.Fixed64Type -> "Fixed64"
                        ProtoType.Int32Type -> "Int32"
                        ProtoType.Int64Type -> "Int64"
                        ProtoType.SFixed32Type -> "SFixed32"
                        ProtoType.SFixed64Type -> "SFixed64"
                        ProtoType.SInt32Type -> "SInt32"
                        ProtoType.SInt64Type -> "SInt64"
                        ProtoType.StringType -> "String"
                        ProtoType.UInt32Type -> "UInt32"
                        ProtoType.UInt64Type -> "UInt64"
                    }
                    MemberName(PACKAGE_IO, "compute${name}Size${if (!withTag) "NoTag" else ""}")
                }
            }
        }

        fun getCodeForRequiredSizeForScalarAttributeC(field: ProtoRegularField): CodeBlock {
            return when (val type = field.type) {
                is ProtoType.NonDeclType -> {
                    CodeBlock.of(
                        "%M(%L, %N)",
                        getComputeDataTypeSizeMember(type, true),
                        field.number,
                        field.attributeName
                    )
                }

                is ProtoType.DefType -> {
                    when (type.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> {
                            CodeBlock.of(
                                "%M(%L, %N)",
                                MemberName(
                                    PACKAGE_IO,
                                    "computeMessageSize"
                                ),
                                field.number,
                                field.attributeName
                            )
                        }

                        ProtoType.DefType.DeclarationType.ENUM -> {
                            CodeBlock.of(
                                "%M(%L, %N.%N)",
                                MemberName(PACKAGE_IO, "computeEnumSize"),
                                field.number,
                                field.attributeName,
                                Const.Enum.NUMBER_PROPERTY_NAME
                            )
                        }
                    }
                }
            }
        }
    }
}
