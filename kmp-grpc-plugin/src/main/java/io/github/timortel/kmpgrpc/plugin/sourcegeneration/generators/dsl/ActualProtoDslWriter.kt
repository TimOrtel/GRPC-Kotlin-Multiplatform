package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object ActualProtoDslWriter : ProtoDslWriter(true) {

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addCode("return %T(", message.className)

            val separator = ",\n"

            val fields = message.fields.joinToCodeBlock(separator) { field ->
                add("%N = %N ?: ", field.attributeName, field.attributeName)
                add(field.defaultValue())
            }

            val mapFields = message.mapFields.joinToCodeBlock(separator) { field ->
                add("%N = %N ?: emptyMap()", field.attributeName, field.attributeName)
            }

            val oneOfFields = message.oneOfs.joinToCodeBlock(separator) { oneOf ->
                add(
                    "%N = %N",
                    oneOf.attributeName,
                    oneOf.attributeName
                )
            }

            val extensionBlock = CodeBlock.of(
                "%N = %N.build()",
                Const.Message.Constructor.MessageExtensions.name,
                Const.DSL.MessageExtensions.name
            )

            val blocks = listOf(fields, mapFields, oneOfFields) +
                    if (message.isExtendable) listOf(extensionBlock) else emptyList()

            addCode(
                blocks
                    .filter { it.isNotEmpty() }
                    .joinToCodeBlock(separator) { add(it) }
            )

            if (fields.isNotEmpty() || mapFields.isNotEmpty() || oneOfFields.isNotEmpty() || message.isExtendable) {
                addCode("\n")
            }

            addCode(")")
        }
    }
}