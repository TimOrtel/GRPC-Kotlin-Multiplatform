package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.config.KeepAliveConfig

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
         * If called, the constructed channel will use http.
         */
        fun usePlaintext(): Builder

        /**
         * Inject the given [interceptors] into all calls started using this channel.
         * Interceptors run in reverse order for sending events and in normal order on receiving events.
         */
        fun withInterceptors(vararg interceptors: CallInterceptor): Builder

        /**
         * Configure keep-alive. By default disabled.
         * @param config The keepAlive configuration.
         * @note Supported on JVM/Android and Native targets only. Calling this method has no effect on JS targets.
         */
        fun withKeepAliveConfig(config: KeepAliveConfig): Builder

        /**
         * Adds all given [certificates] to the set of trusted root certificates used
         * by this channel. Both CA certificates and self-signed/leaf certificates are
         * accepted.
         *
         * This method has no effect on JavaScript targets.
         */
        fun withTrustedCertificates(vararg certificates: Certificate): Builder

        /**
         * Adds all given [certificates] to the set of trusted root certificates used
         * by this channel. Both CA certificates and self-signed/leaf certificates are
         * accepted.
         *
         * This method has no effect on JavaScript targets.
         */
        fun withTrustedCertificates(certificates: List<Certificate>): Builder

        /**
         * Configures the channel to trust only the certificates explicitly provided via
         * [withTrustedCertificates]. System or platform root certificates will not be used.
         *
         * This method has no effect on JavaScript targets.
         */
        fun trustOnlyProvidedCertificates(): Builder

        /**
         * Construct the channel
         */
        fun build(): Channel
    }

    /**
     * True after either [shutdown] or [shutdownNow] have been called, this channel has no running RPCs and its
     * resources have been cleaned up.
     */
    val isTerminated: Boolean

    /**
     * Initiates an orderly shutdown of the gRPC channel. After this method is called, no new calls can be started
     * using the channel. However, existing calls will continue until they are completed or canceled.
     *
     * Suspends until [isTerminated] is true. If the calling coroutine is canceled before this call returns,
     * memory leaks are possible.
     */
    suspend fun shutdown()

    /**
     * Initiates a forceful shutdown of the gRPC channel. After invoking this method, no new calls
     * can be started, and ongoing calls are immediately canceled.
     *
     * This method is used to terminate the channel abruptly, without waiting for ongoing calls
     * to complete gracefully. It should be used with caution, as it can interrupt active
     * operations and cause incomplete responses.
     *
     * Suspends until [isTerminated] is true. If the calling coroutine is canceled before this call returns,
     * memory leaks are possible.
     */
    suspend fun shutdownNow()
}
