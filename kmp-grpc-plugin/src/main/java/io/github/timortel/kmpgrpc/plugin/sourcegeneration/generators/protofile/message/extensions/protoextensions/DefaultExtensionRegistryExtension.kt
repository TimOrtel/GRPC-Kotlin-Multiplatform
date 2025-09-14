package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.protoextensions

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmExtensionRegistry
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.MessageWriterExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.joinCodeBlocks

object DefaultExtensionRegistryExtension : MessageWriterExtension {

    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addProperty(
            Const.Message.Companion.defaultExtensionRegistryProperty
                .parametrizedBy(message.className)
                .toPropertySpecBuilder(KModifier.OVERRIDE)
                .apply {
                    if (sourceTarget is SourceTarget.Actual) {
                        addModifiers(KModifier.ACTUAL)

                        val initializer = CodeBlock.builder()
                            .add("%T.of(", kmExtensionRegistry)
                            .apply {
                                val extensionsCodeBlocks = message.extensionsInProject.flatMap { extensionDefinition ->
                                    extensionDefinition.fields.map { field ->
                                        CodeBlock.of("%M", field.memberName)
                                    }
                                }

                                add(extensionsCodeBlocks.joinCodeBlocks(", "))
                            }
                            .add(")")
                            .build()

                        initializer(initializer)
                    }
                }
                .build()
        )
    }

    override fun appliesTo(message: ProtoMessage, sourceTarget: SourceTarget): Boolean {
        return message.isExtendable
    }
}
