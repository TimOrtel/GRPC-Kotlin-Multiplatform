package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.functions

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.MessageWriterExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

object ToStringFunctionExtension : MessageWriterExtension {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        if (sourceTarget is SourceTarget.Actual) {
            builder.addFunction(
                FunSpec
                    .builder(Const.Message.BasicFunctions.ToStringFunction.NAME)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(STRING)
                    .apply {
                        beginControlFlow("return buildString")

                        addStatement("append(%S)", message.className.simpleName)
                        addStatement("append(%S)", "{")

                        val separator = CodeBlock.of("\nappend(%S)\n", ", ")

                        val propertiesCodeBlock =
                            (message.fields + message.oneOfs + message.mapFields).joinToCodeBlock(separator) { field ->
                                addStatement("append(%S)", field.attributeName)
                                addStatement("append(%S)", "=")
                                addStatement("append(%N.toString())", field.attributeName)
                            }

                        val unknownFieldsCodeBlock = CodeBlock.builder()
                            .apply {
                                addStatement("append(%S)", Const.Message.Constructor.UnknownFields.name)
                                addStatement("append(%S)", "=")
                                addStatement("append(%N.toString())", Const.Message.Constructor.UnknownFields.name)
                            }
                            .build()

                        val extensionsCodeBlock = CodeBlock.builder()
                            .apply {
                                addStatement("append(%S)", Const.Message.Constructor.MessageExtensions.name)
                                addStatement("append(%S)", "=")
                                addStatement("append(%N.toString())", Const.Message.Constructor.MessageExtensions.name)
                            }
                            .build()

                        val codeBlocks =
                            listOf(propertiesCodeBlock, unknownFieldsCodeBlock) +
                                    if (message.isExtendable) listOf(extensionsCodeBlock) else emptyList()

                        addCode(codeBlocks.joinCodeBlocks(separator))

                        addStatement("append(%S)", "}")

                        endControlFlow()
                    }
                    .build()
            )
        }
    }
}
