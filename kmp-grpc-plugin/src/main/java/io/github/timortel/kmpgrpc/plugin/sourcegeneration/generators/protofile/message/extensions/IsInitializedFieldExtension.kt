package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.isLegacyRequired
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

object IsInitializedFieldExtension : MessageWriterExtension {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val isActual = sourceTarget is SourceTarget.Actual

        builder.addProperty(
            Const.Message.isInitializedProperty.toPropertySpecBuilder(KModifier.OVERRIDE)
                .apply {
                    if (isActual) {
                        addModifiers(KModifier.ACTUAL)

                        initializer(
                            CodeBlock.builder().apply {
                                val requiredFields = message.fields.filter { field ->
                                    field.cardinality.isLegacyRequired
                                }

                                val subMessageFields = message.fields.filter { it.type.isMessage }
                                val subMessageMapFields = message.mapFields.filter { it.valuesType.isMessage }
                                val oneOfs = message.oneOfs.filter { oneOf -> oneOf.fields.any { it.type.isMessage } }

                                val subMessages = subMessageFields + subMessageMapFields + oneOfs

                                val consideredExtensionFields = message.extensionsInProject.flatMap { extensions ->
                                    extensions.fields.filter { field ->
                                        field.cardinality.isLegacyRequired || field.type.isMessage
                                    }
                                }

                                if (requiredFields.isEmpty() && subMessages.isEmpty() && consideredExtensionFields.isEmpty()) {
                                    add("true")
                                } else {
                                    val separator = " && "
                                    val requiredFieldsBool = requiredFields.joinToCodeBlock(separator) {
                                        add("%N", it.isSetProperty.name)
                                    }

                                    val subMessageFieldsBool = subMessageFields.joinToCodeBlock(separator) {
                                        when (it.cardinality) {
                                            is ProtoFieldCardinality.Singular -> {
                                                add(
                                                    "(%1N == null || %1N.%2N)",
                                                    it.codeName,
                                                    Const.Message.isInitializedProperty.name
                                                )
                                            }

                                            ProtoFieldCardinality.Repeated -> {
                                                add(
                                                    "%N.all { it.%N }",
                                                    it.codeName,
                                                    Const.Message.isInitializedProperty.name
                                                )
                                            }
                                        }
                                    }

                                    val subMessageOneOfFieldsBool = oneOfs.joinToCodeBlock(separator) {
                                        add(
                                            "%N.%N",
                                            it.codeName,
                                            Const.Message.OneOf.isInitializedProperty.name
                                        )
                                    }

                                    val subMessageMapFieldsBool = subMessageMapFields.joinToCodeBlock(separator) {
                                        add(
                                            "%N.values.all { it.%N }",
                                            it.codeName,
                                            Const.Message.isInitializedProperty.name
                                        )
                                    }

                                    val requiredExtensionFieldsBool =
                                        consideredExtensionFields.joinToCodeBlock(separator) { field ->
                                            val extensionMember = MemberName(field.file.className, field.codeName)

                                            when (field.cardinality) {
                                                is ProtoFieldCardinality.Singular -> {
                                                    if (field.type.isMessage) {
                                                        add(
                                                            "%N[%M]?.%N == true",
                                                            Const.Message.Constructor.MessageExtensions.name,
                                                            extensionMember,
                                                            Const.Message.isInitializedProperty.name
                                                        )
                                                    } else {
                                                        add(
                                                            "%N[%M] != null",
                                                            Const.Message.Constructor.MessageExtensions.name,
                                                            extensionMember
                                                        )
                                                    }
                                                }

                                                ProtoFieldCardinality.Repeated -> {
                                                    if (field.type.isMessage) {
                                                        add(
                                                            "%N[%M].all { it.%N }",
                                                            Const.Message.Constructor.MessageExtensions.name,
                                                            extensionMember,
                                                            Const.Message.isInitializedProperty.name
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                    val impl = listOf(
                                        requiredFieldsBool,
                                        subMessageFieldsBool,
                                        subMessageOneOfFieldsBool,
                                        subMessageMapFieldsBool,
                                        requiredExtensionFieldsBool
                                    ).joinCodeBlocks(separator)

                                    add(impl)
                                }
                            }
                                .build()
                        )
                    }
                }
                .build()
        )
    }
}
