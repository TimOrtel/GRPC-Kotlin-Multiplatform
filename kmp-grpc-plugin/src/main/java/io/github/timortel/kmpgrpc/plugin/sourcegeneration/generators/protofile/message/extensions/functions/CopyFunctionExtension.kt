package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.functions

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.MessageWriterExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

/**
 * Adds a copy function to messages, similar to Kotlin data classes.
 */
object CopyFunctionExtension : MessageWriterExtension {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val isActual = sourceTarget is SourceTarget.Actual

        builder.addFunction(
            FunSpec.builder("copy")
                .returns(message.className)
                .apply {
                    (message.fields + message.mapFields + message.oneOfs)
                        .forEach { messageProperty ->
                            addParameter(
                                ParameterSpec.builder(
                                    name = messageProperty.attributeName,
                                    type = messageProperty.propertyType
                                )
                                    .apply {
                                        if (!isActual) defaultValue("this.%N", messageProperty.attributeName)
                                    }
                                    .build()
                            )
                        }

                    addParameter(
                        Const.Message.Constructor.UnknownFields.toParamSpecBuilder()
                            .apply {
                                if (!isActual) defaultValue("this.%N", Const.Message.Constructor.UnknownFields.name)
                            }
                            .build()
                    )

                    if (isActual) {
                        addModifiers(KModifier.ACTUAL)
                        writeCopyFunctionCode(this, message)
                    }
                }
                .build()
        )
    }

    private fun writeCopyFunctionCode(builder: FunSpec.Builder, message: ProtoMessage) {
        builder
            .addCode(
                CodeBlock
                    .builder()
                    .add("return %T(", message.className)
                    .indent()
                    .apply {
                        (message.fields + message.mapFields + message.oneOfs)
                            .forEach { messageProperty ->
                                add("%1N = %1N,\n", messageProperty.attributeName)
                            }
                        add("%1N = %1N\n", Const.Message.Constructor.UnknownFields.name)
                    }
                    .unindent()
                    .add(")")
                    .build()
            )
    }
}
