package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

/**
 * Base interface of all nodes that may hold extension definitions.
 */
interface ProtoExtensionDefinitionHolder {
    val extensionDefinitions: List<ProtoExtensionDefinition>
}
