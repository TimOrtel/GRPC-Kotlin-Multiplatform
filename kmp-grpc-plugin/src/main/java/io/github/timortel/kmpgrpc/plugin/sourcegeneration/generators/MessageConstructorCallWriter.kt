package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

object MessageConstructorCallWriter {

    enum class ConstructorType {
        DIRECT,
        BUILD,
        BUILD_PARTIAL
    }

    fun getConstructorCallCode(
        message: ProtoMessage,
        type: ConstructorType,
        getFieldParameter: (ProtoMessageField) -> CodeBlock,
        getMapFieldParameter: (ProtoMapField) -> CodeBlock,
        getOneOfFieldParameter: (ProtoOneOf) -> CodeBlock,
        getUnknownFieldsParameter: () -> CodeBlock?,
        getExtensionParameter: () -> CodeBlock,
    ): CodeBlock {
        return CodeBlock.builder()
            .apply {
                val companion = message.className.nestedClass("Companion")

                when (type) {
                    ConstructorType.DIRECT -> add("%T(", message.className)
                    ConstructorType.BUILD -> add("%M(", companion.member("invoke"))
                    ConstructorType.BUILD_PARTIAL -> add("%M(", companion.member("createPartial"))
                }

                add("\n")
                indent()

                val separator = ",\n"

                val fields = message.fields.joinToCodeBlock(separator) { field ->
                    add("%N = ", field.attributeName)
                    add(getFieldParameter(field))
                }

                val mapFields = message.mapFields.joinToCodeBlock(separator) { field ->
                    add("%N = ", field.attributeName)
                    add(getMapFieldParameter(field))
                }

                val oneOfFields = message.oneOfs.joinToCodeBlock(separator) { oneOf ->
                    add("%N = ", oneOf.attributeName)
                    add(getOneOfFieldParameter(oneOf))
                }

                val extensionBlock = CodeBlock.builder()
                    .add("%N = ", Const.Message.Constructor.MessageExtensions.name)
                    .add(getExtensionParameter())
                    .build()

                val unknownFields = getUnknownFieldsParameter()?.let {
                    listOf(
                        CodeBlock.builder()
                            .add("%N = ", Const.Message.Constructor.UnknownFields.name)
                            .add(it)
                            .build()
                    )
                }.orEmpty()

                val blocks = listOf(fields, mapFields, oneOfFields) + unknownFields +
                        if (message.isExtendable) listOf(extensionBlock) else emptyList()

                add(
                    blocks
                        .filter { it.isNotEmpty() }
                        .joinToCodeBlock(separator) { add(it) }
                )

                if (fields.isNotEmpty() || mapFields.isNotEmpty() || oneOfFields.isNotEmpty() || message.isExtendable) {
                    add("\n")
                }

                unindent()
                add(")")
            }
            .build()
    }
}
