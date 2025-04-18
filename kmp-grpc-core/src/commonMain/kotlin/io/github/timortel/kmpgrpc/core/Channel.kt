package io.github.timortel.kmpgrpc.core

/**
 * Wraps around the gRPC-Channel. Create a channel using [Builder.forAddress].
 * For more information about grpc channels, please refer to [the official grpc channel documentation](https://grpc.io/docs/what-is-grpc/core-concepts/#channels).
 */
expect class Channel {
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
         * Interceptors run in reverse order for sending events and in normal order on receiving events.
         */
        fun withInterceptors(vararg interceptors: CallInterceptor): Builder

        /**
         * Construct the channel
         */
        fun build(): Channel
    }
}
