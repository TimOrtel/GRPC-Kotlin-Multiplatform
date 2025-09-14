package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

interface ProtoExtensionDefinitionFinder {

    /**
     * @return the list of all proto extension definitions defined in this node and all child nodes
     */
    fun findExtensionDefinitions(): List<ProtoExtensionDefinition>
}
