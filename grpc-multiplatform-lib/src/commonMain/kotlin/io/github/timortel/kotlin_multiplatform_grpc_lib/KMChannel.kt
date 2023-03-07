package io.github.timortel.kotlin_multiplatform_grpc_lib

/**
 * Wrapps around the GRPC-Channel
 */
expect class KMChannel {
    class Builder {
        companion object {
            fun forAddress(name: String, port: Int): Builder
        }

        fun usePlaintext(): Builder

        fun build(): KMChannel
    }
}