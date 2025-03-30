package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

interface ProtoNode {

    /**
     * Validate that this node and all of its children do not violate any proto constraints.
     */
    fun validate()
}
