package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model

interface ProtoNode {

    /**
     * Validate that this node and all of its children do not violate any proto constraints.
     */
    fun validate()
}
