package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.functions

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.MessageWriterExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

/**
 * Write the equals function to proto messages.
 */
object EqualsFunctionExtension : MessageWriterExtension {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val isActual = sourceTarget is SourceTarget.Actual

        builder.addFunction(
            FunSpec.builder(Const.Message.BasicFunctions.EqualsFunction.NAME)
                .addModifiers(
                    if (isActual) KModifier.ACTUAL else KModifier.EXPECT,
                    KModifier.OVERRIDE
                )
                .addParameter(
                    Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM,
                    ANY.copy(nullable = true)
                )
                .returns(BOOLEAN)
                .apply {
                    if (isActual) {
                        writeEqualsFunction(this, message)
                    }
                }
                .build()
        )
    }

    private fun writeEqualsFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            val otherParamName = Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM

            addStatement("if (%N === this) return true", otherParamName)
            addStatement("if (%N !is %T) return false", otherParamName, message.className)

            val separator = "\n"

            val fieldsCodeBlock = message.fields.joinToCodeBlock(separator) { field ->
                add("if (")
                add(
                    field.type.inequalityCode(
                        attributeName = field.attributeName,
                        otherParamName = otherParamName,
                        isRepeated = field.cardinality == ProtoFieldCardinality.Repeated
                    )
                )
                add(") return false")
            }

            val mapFieldCodeBlock = message.mapFields.joinToCodeBlock(separator) { mapField ->
                add(
                    "if (%1N != %2N.%1N) return false",
                    mapField.attributeName,
                    otherParamName
                )
            }

            // Assume that each one of sealed class has their equals method set properly
            val oneOfCodeBlock = message.oneOfs.joinToCodeBlock(separator) { oneOf ->
                add(
                    "if (%1N != %2N.%1N) return false",
                    oneOf.attributeName,
                    otherParamName
                )
            }

            val unknownFieldsBlock = CodeBlock.of(
                "if (%1N != %2N.%1N) return false",
                Const.Message.Constructor.UnknownFields.name,
                otherParamName
            )

            addCode(
                listOf(fieldsCodeBlock, mapFieldCodeBlock, oneOfCodeBlock, unknownFieldsBlock).joinCodeBlocks(separator)
            )

            addCode("\n")

            addStatement("return true")
        }
    }
}
