package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.joinToCodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage

/**
 * For JVM, JS and iOS
 */
object ActualProtoDslWriter : ProtoDslWriter(true) {

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addCode("return %T(", message.className)

            val separator = ",\n"

            val fields = message.fields.joinToCodeBlock(separator) { field ->
                add("%N = %N ?: ", field.fieldName, field.fieldName)
                add(field.defaultValue())
            }

            val mapFields = message.mapFields.joinToCodeBlock(separator) { field ->
                add("%N = %N ?: emptyMap()", field.fieldName, field.fieldName)
            }

            val oneOfFields = message.oneOfs.joinToCodeBlock(separator) { oneOf ->
                add(
                    "%N = %N",
                    oneOf.fieldName,
                    oneOf.fieldName
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