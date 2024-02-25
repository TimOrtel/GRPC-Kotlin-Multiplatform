package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

abstract class IosJvmProtoFileWriteBase(protoFile: ProtoFile) : ActualProtoFileWriter(protoFile) {

    override fun applyToClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        messageClassName: ClassName
    ) {
        super.applyToClass(builder, message, messageClassName)

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
                val onlyNonOneOfAttributes = message.attributes.filter { !it.isOneOfAttribute }

                onlyNonOneOfAttributes.forEachIndexed { i, attr ->
                    if (i != 0) add("·+\n")
                    when (attr.attributeType) {
                        is Scalar -> {
                            add("if·(%N·==·", Const.Message.Attribute.propertyName(message, attr))
                            add(attr.commonDefaultValue(false, useEmptyMessage = false))
                            add(")·0·else·{ ")
                            add(getCodeForRequiredSizeForScalarAttributeC(attr))
                            add(" }")
                        }

                        is Repeated -> {
                            val attrFieldName =
                                Const.Message.Attribute.Repeated.listPropertyName(attr)

                            add(
                                "if (%N.isEmpty()) 0 else %N.let·{·e ->\n",
                                attrFieldName,
                                attrFieldName
                            )
                            beginControlFlow("val dataSize = e.sumOf·{")
                            when (attr.types.protoType) {
                                ProtoType.DOUBLE,
                                ProtoType.FLOAT,
                                ProtoType.INT_32,
                                ProtoType.INT_64,
                                ProtoType.BOOL,
                                ProtoType.STRING -> add(
                                    "%M(it)",
                                    getComputeDataTypeSizeMember(attr.types.protoType, false)
                                )

                                ProtoType.MESSAGE -> add(
                                    "%M(it)",
                                    MemberName(
                                        PACKAGE_IO,
                                        "computeMessageSizeNoTag"
                                    )
                                )

                                ProtoType.ENUM -> add(
                                    "%M(it.%N)",
                                    MemberName(PACKAGE_IO, "computeEnumSizeNoTag"),
                                    Const.Enum.VALUE_PROPERTY_NAME
                                )

                                ProtoType.MAP -> throw IllegalStateException()
                            }
                            endControlFlow()
                            addStatement("val tagSize = %M(%L)", ComputeTagSize, attr.protoId)

                            val isPackable = isAttrPackable(attr)

                            if (isPackable) {
                                addStatement(
                                    "dataSize + tagSize + %M(tagSize)",
                                    ComputeInt32SizeNoTag
                                )
                            } else {
                                addStatement("dataSize + %N.size * tagSize", attrFieldName)
                            }

                            add("\n}")
                        }

                        is MapType -> {
                            add(
                                "%M(%L, %N, ::%M, ",
                                computeMapSize,
                                attr.protoId,
                                Const.Message.Attribute.propertyName(message, attr),
                                getComputeDataTypeSizeMember(
                                    attr.attributeType.keyTypes.protoType,
                                    true
                                )
                            )

                            add(getComputeMapValueRequiredSizeCode(attr.attributeType.valueTypes))

                            add(")")
                        }
                    }
                }

                message.oneOfs.forEachIndexed { index, oneOf ->
                    if (index != 0 || onlyNonOneOfAttributes.isNotEmpty()) add("·+\n")
                    add(
                        "%N.%N",
                        Const.Message.OneOf.propertyName(message, oneOf),
                        Const.Message.OneOf.IosJvm.REQUIRED_SIZE_PROPERTY_NAME
                    )
                }

                // Fallback for messages without any fields.
                if (onlyNonOneOfAttributes.isEmpty() && message.oneOfs.isEmpty()) {
                    add("0")
                }
            }
            .build()
    }

    override fun buildMapAttributeSerializeCode(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageAttribute,
        mapType: MapType
    ) {
        builder.apply {
            addCode(
                "%M(%N, %L, %N, ::%M, ",
                writeMap,
                Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                attr.protoId,
                Const.Message.Attribute.propertyName(message, attr),
                getComputeDataTypeSizeMember(
                    mapType.keyTypes.protoType,
                    true
                ),
            )

            addCode(getComputeMapValueRequiredSizeCode(mapType.valueTypes))

            addCode(", ")

            addMapKeyTypeSerializationCode(mapType.keyTypes)
            addCode(", ")
            addMapValueTypeSerializationCode(mapType.valueTypes)
            addCode(")\n")
        }
    }

    private fun getComputeMapValueRequiredSizeCode(valueTypes: Types): CodeBlock {
        return when (valueTypes.protoType) {
            ProtoType.DOUBLE,
            ProtoType.FLOAT,
            ProtoType.INT_32,
            ProtoType.INT_64,
            ProtoType.BOOL,
            ProtoType.STRING, ProtoType.MESSAGE -> CodeBlock.of(
                "::%M",
                getComputeDataTypeSizeMember(valueTypes.protoType, true)
            )

            ProtoType.ENUM -> CodeBlock.of(
                "{·fieldNumber,·it·-> %M(fieldNumber, it.%N)·}",
                getComputeDataTypeSizeMember(valueTypes.protoType, true),
                Const.Enum.VALUE_PROPERTY_NAME
            )

            ProtoType.MAP -> throw IllegalStateException()
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
                ProtoType.DOUBLE,
                ProtoType.FLOAT,
                ProtoType.INT_32,
                ProtoType.INT_64,
                ProtoType.BOOL,
                ProtoType.STRING,
                ProtoType.ENUM -> {
                    val name = when (protoType) {
                        ProtoType.DOUBLE -> "Double"
                        ProtoType.FLOAT -> "Float"
                        ProtoType.INT_32 -> "Int32"
                        ProtoType.INT_64 -> "Int64"
                        ProtoType.BOOL -> "Bool"
                        ProtoType.STRING -> "String"
                        ProtoType.ENUM -> "Enum"
                        ProtoType.MAP, ProtoType.MESSAGE -> throw IllegalStateException()
                    }
                    MemberName(PACKAGE_IO, "compute${name}Size${if (!withTag) "NoTag" else ""}")
                }

                ProtoType.MESSAGE -> computeMessageSize
                ProtoType.MAP -> computeMapSize
            }
        }

        fun getCodeForRequiredSizeForScalarAttributeC(attr: ProtoMessageAttribute): CodeBlock =
            when (attr.types.protoType) {
                ProtoType.DOUBLE,
                ProtoType.FLOAT,
                ProtoType.INT_32,
                ProtoType.INT_64,
                ProtoType.BOOL,
                ProtoType.STRING -> {
                    CodeBlock.of(
                        "%M(%L, %N)",
                        getComputeDataTypeSizeMember(attr.types.protoType, true),
                        attr.protoId,
                        attr.name
                    )
                }

                ProtoType.MESSAGE -> CodeBlock.of(
                    "%M(%L, %N)",
                    MemberName(
                        PACKAGE_IO,
                        "computeMessageSize"
                    ),
                    attr.protoId,
                    attr.name
                )

                ProtoType.ENUM -> CodeBlock.of(
                    "%M(%L, %N.%N)",
                    MemberName(PACKAGE_IO, "computeEnumSize"),
                    attr.protoId,
                    attr.name,
                    Const.Enum.VALUE_PROPERTY_NAME
                )

                ProtoType.MAP -> throw IllegalStateException()
            }
    }
}