package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.extensions

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmExtensionRegistry
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmExtensionRepeated
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmExtensionRepeatedNonPackable
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmExtensionRepeatedPackable
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmExtensionScalar
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.shared.internal.io.wireFormatMakeTag

object ProtoExtensionWriter {

    fun writeExtensions(
        builder: TypeSpec.Builder,
        extensionDefinitions: List<ProtoExtensionDefinition>,
        isActual: Boolean
    ) {
        builder.apply {
            extensionDefinitions.forEach { extensionDefinition ->
                extensionDefinition.fields.forEach { field ->
                    val messageTypeName = extensionDefinition.messageType.resolve()
                    val valueTypeName = field.type.resolve()

                    val parentName = when (val p = extensionDefinition.parent) {
                        is ProtoExtensionDefinition.Parent.File -> p.file.protoPackage.packageIdentifier
                        is ProtoExtensionDefinition.Parent.Message -> p.message.fullName
                    }

                    val fullName = "$parentName.${field.name}"

                    val propertyType = when (field.cardinality) {
                        is ProtoFieldCardinality.Singular -> kmExtensionScalar
                        ProtoFieldCardinality.Repeated -> {
                            kmExtensionRepeated
                        }
                    }.parameterizedBy(messageTypeName, valueTypeName)

                    addProperty(
                        PropertySpec.builder(
                            name = field.codeName,
                            type = propertyType,
                            modifiers = if (isActual) listOf(KModifier.ACTUAL) else emptyList()
                        )
                            .apply {
                                if (isActual) {

                                    initializer(
                                        buildPropertyInitializer(
                                            field = field,
                                            messageTypeName = messageTypeName,
                                            valueTypeName = valueTypeName,
                                            fullName = fullName
                                        )
                                    )
                                }
                            }
                            .build()
                    )
                }
            }
        }
    }

    private fun buildPropertyInitializer(
        field: ProtoMessageField,
        messageTypeName: TypeName,
        valueTypeName: TypeName,
        fullName: String,
    ): CodeBlock {
        val fieldTypeBlock = when (field.type) {
            is ProtoType.DefType -> {
                when (val decl = field.type.resolveDeclaration()) {
                    is ProtoMessage -> {
                        CodeBlock.builder()
                            .add(
                                "%1T(%2T.Companion, ",
                                field.type.fieldType,
                                field.type.resolve(),
                            )
                            .apply {
                                if (decl.isExtendable) {
                                    add(
                                        "%T.Companion.%N",
                                        field.type.resolve(),
                                        Const.Message.Companion.defaultExtensionRegistryProperty.name
                                    )
                                } else {
                                    add("%T.empty()", kmExtensionRegistry)
                                }
                            }
                            .add(")")
                            .build()
                    }

                    else -> {
                        CodeBlock.of(
                            "%T(%T.Companion)",
                            field.type.fieldType,
                            field.type.resolve()
                        )
                    }
                }
            }

            else -> {
                CodeBlock.of("%T", field.type.fieldType)
            }
        }

        return if (field.cardinality is ProtoFieldCardinality.Singular || !field.type.isPackable) {
            val extension = when (field.cardinality) {
                is ProtoFieldCardinality.Singular -> kmExtensionScalar
                is ProtoFieldCardinality.Repeated -> kmExtensionRepeatedNonPackable
            }

            CodeBlock.builder()
                .add(
                    "%T(%S, %L, %T::class, ",
                    extension.parameterizedBy(messageTypeName, valueTypeName),
                    fullName,
                    field.number,
                    messageTypeName
                )
                .add(fieldTypeBlock)
                .add(")")
                .build()
        } else {
            CodeBlock.builder()
                .add(
                    "%T(%S, %L, %T::class, ",
                    kmExtensionRepeatedPackable.parameterizedBy(messageTypeName, valueTypeName),
                    fullName,
                    field.number,
                    messageTypeName,
                )
                .add(fieldTypeBlock)
                .add(
                    ", %L, %Lu)",
                    field.isPacked,
                    wireFormatMakeTag(field.number, field.type.wireType, true)
                )
                .build()
        }
    }
}
