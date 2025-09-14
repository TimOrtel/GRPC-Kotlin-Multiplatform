package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoRegularField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

/**
 * Adds the requiredSize property for IOS and JVM targets.
 */
class RequiredSizePropertyExtension : BaseSerializationExtension() {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addProperty(
            PropertySpec
                .builder("requiredSize", INT, KModifier.OVERRIDE)
                .apply {
                    if (sourceTarget is SourceTarget.Actual) {
                        addModifiers(KModifier.ACTUAL)

                        initializer(buildRequiredSizeInitializer(message))
                    }
                }
                .build()
        )
    }

    private fun buildRequiredSizeInitializer(message: ProtoMessage): CodeBlock {
        return CodeBlock
            .builder()
            .apply {
                val separator = "·+\n"

                val fieldsCodeBlock = message.fields.joinToCodeBlock(separator) { field ->
                    when {
                        field.cardinality == ProtoFieldCardinality.Optional || (field.type.isMessage && field.cardinality != ProtoFieldCardinality.Repeated) -> {
                            add("if·(%N)·{·", field.isSetProperty.attributeName)
                            add(
                                getCodeForRequiredSizeForScalarAttributeC(
                                    field,
                                    field.cardinality == ProtoFieldCardinality.Optional
                                )
                            )
                            add("}·else·{·0·}")
                        }

                        field.cardinality == ProtoFieldCardinality.Implicit -> {
                            add("if·(")
                            add(field.type.isDefaultValueCode(field.attributeName, false))
                            add(")·0·else·{ ")
                            add(getCodeForRequiredSizeForScalarAttributeC(field, isOptional = false))
                            add(" }")
                        }

                        field.cardinality == ProtoFieldCardinality.Repeated -> {
                            add("if·(")
                            add(field.type.isDefaultValueCode(field.attributeName, true))
                            beginControlFlow(")·0·else·{")

                            beginControlFlow("%N.let·{·e·->", field.attributeName)

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
                                                computeMessageSizeNoTag
                                            )
                                        }

                                        ProtoType.DefType.DeclarationType.ENUM -> {
                                            add(
                                                "%M(it.%N)",
                                                computeEnumSizeNoTag,
                                                Const.Enum.NUMBER_PROPERTY_NAME
                                            )
                                        }
                                    }
                                }
                            }
                            endControlFlow()
                            addStatement("val tagSize = %M(%L)", computeTagSize, field.number)

                            if (field.type.isPackable) {
                                addStatement(
                                    "dataSize + tagSize + %M(tagSize)",
                                    computeInt32SizeNoTag
                                )
                            } else {
                                addStatement("dataSize + %N.size * tagSize", field.attributeName)
                            }

                            endControlFlow()
                            unindent()
                            add("}")
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

                val unknownFieldsBlock = CodeBlock.of(
                    "%M(%N)",
                    computeUnknownFieldsRequiredSize,
                    Const.Message.Constructor.UnknownFields.name
                )

                val extensionsBlock = CodeBlock.of(
                    "%N.requiredSize",
                    Const.Message.Constructor.MessageExtensions.name
                )

                val blocks = listOf(fieldsCodeBlock, mapFieldsCodeBlock, oneOfsBlock, unknownFieldsBlock) +
                        if (message.isExtendable) listOf(extensionsBlock) else emptyList()

                add(blocks.joinCodeBlocks(separator))
            }
            .build()
    }

    companion object {
        fun getCodeForRequiredSizeForScalarAttributeC(field: ProtoRegularField, isOptional: Boolean): CodeBlock {
            return when (val type = field.type) {
                is ProtoType.NonDeclType -> {
                    // For optional fields, 'this' is needed as otherwise the constructor parameter is accessed, which is nullable
                    val formatString = if (isOptional) "%M(%L, this.%N)" else "%M(%L, %N)"

                    CodeBlock.of(
                        formatString,
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
                                computeMessageSize,
                                field.number,
                                field.attributeName
                            )
                        }

                        ProtoType.DefType.DeclarationType.ENUM -> {
                            CodeBlock.of(
                                "%M(%L, %N.%N)",
                                computeEnumSize,
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
