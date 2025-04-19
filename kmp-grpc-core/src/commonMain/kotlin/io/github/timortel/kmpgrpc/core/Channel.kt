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

    /**
     * Initiates an orderly shutdown of the gRPC channel. After this method is called, no new calls can be started
     * using the channel. However, existing calls will continue until they are completed or canceled.
     */
    fun shutdown()

    /**
     * Initiates a forceful shutdown of the gRPC channel. After invoking this method, no new calls
     * can be started, and ongoing calls are immediately canceled.
     *
     * This method is used to terminate the channel abruptly, without waiting for ongoing calls
     * to complete gracefully. It should be used with caution, as it can interrupt active
     * operations and cause incomplete responses.
     */
    fun shutdownNow()
}
