package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.KMChannel.Builder

/**
 * Wraps around the gRPC-Channel. Create a channel using [Builder.forAddress].
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
         * Inject the given [interceptors] into all calls started using this channel.
         * The [interceptors] are called in reverse order.
         */
        fun withInterceptors(vararg interceptors: CallInterceptor): Builder

        /**
         * Construct the channel
         */
        fun build(): KMChannel
    }
}
