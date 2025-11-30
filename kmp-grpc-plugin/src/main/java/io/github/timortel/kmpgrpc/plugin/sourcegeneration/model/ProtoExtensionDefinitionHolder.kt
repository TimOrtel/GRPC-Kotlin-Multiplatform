package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

/**
 * Base interface of all nodes that may hold extension definitions.
 */
interface ProtoExtensionDefinitionHolder : ProtoNode, ProtoExtensionDefinitionFinder {
    val extensionDefinitions: List<ProtoExtensionDefinition>

    val file: ProtoFile

    /**
     * The child messages of this proto extension definition holder
     */
    val messages: List<ProtoMessage>

    override fun validate() {
        extensionDefinitions.forEach { it.validate() }

        // Test that a single holder does not have name clashes
        extensionDefinitions
            .flatMap { it.fields }
            .groupBy { it.name }
            .filter { (_, values) ->
                values.size > 1
            }
            .forEach { (name, values) ->
                val message = buildString {
                    append("Duplicated extension field definitions detected for $name:\n")
                    append(
                        values.joinToString("\n") { "-> ${it.ctx.text} at ${it.ctx.toFilePositionString(file.path)}" }
                    )
                }

                throw CompilationException.DuplicateDeclaration(message, file)
            }
    }

    override fun findExtensionDefinitions(): List<ProtoExtensionDefinition> {
        return messages.flatMap { it.findExtensionDefinitions() } + extensionDefinitions
    }
}
