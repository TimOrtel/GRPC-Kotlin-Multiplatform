package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinToCodeBlock

object ToStringFunctionExtension : MessageWriterExtension {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        if (sourceTarget is SourceTarget.Actual) {
            builder.addFunction(
                FunSpec
                    .builder("toString")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(STRING)
                    .apply {
                        beginControlFlow("return buildString")

                        addStatement("append(%S)", message.className.simpleName)
                        addStatement("append(%S)", "{")

                        val separator = CodeBlock.of("\nappend(%S)\n", ", ")

                        val propertiesCodeBlock = (message.fields + message.oneOfs + message.mapFields).joinToCodeBlock(separator) { field ->
                            addStatement("append(%S)", field.attributeName)
                            addStatement("append(%S)", "=")
                            addStatement("append(%N.toString())", field.attributeName)
                        }

                        addCode(propertiesCodeBlock)

                        addStatement("append(%S)", "}")

                        endControlFlow()
                    }
                    .build()
            )
        }
    }
}