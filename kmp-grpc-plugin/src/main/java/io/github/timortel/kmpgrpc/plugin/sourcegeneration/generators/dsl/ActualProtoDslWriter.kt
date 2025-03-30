package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

/**
 * For JVM, JS and iOS
 */
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

            addCode(
                listOf(fields, mapFields, oneOfFields)
                    .filter { it.isNotEmpty() }
                    .joinToCodeBlock(separator) { add(it) }
            )

            if (fields.isNotEmpty() || mapFields.isNotEmpty() || oneOfFields.isNotEmpty()) addCode("\n")

            addCode(")")
        }
    }
}