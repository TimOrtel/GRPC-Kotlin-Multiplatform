package io.github.timortel.kmpgrpc.core

/**
 * Wrapps around the GRPC-Channel. Create a channel using [Builder.forAddress].
 * For more information about grpc channels please refer to [the official grpc channel documentation](https://grpc.io/docs/what-is-grpc/core-concepts/#channels).
 */
expect class KMChannel {
    class Builder {
        companion object {
            /**
             * Construct a new [Builder]. Specify [name] and [port] that direct to your server.
             */
            fun forAddress(name: String, port: Int): Builder
        }

        /**
         * If called, the constructed channel will use http-
         */
        fun usePlaintext(): Builder

        /**
         * Construct the channel
         */
        fun build(): KMChannel
    }
}