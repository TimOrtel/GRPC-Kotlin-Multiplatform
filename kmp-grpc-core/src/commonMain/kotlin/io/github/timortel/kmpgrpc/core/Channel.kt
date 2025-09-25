package io.github.timortel.kmpgrpc.core

import kotlin.time.Duration

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
         * Sets the keepalive time, the delay between keepalive pings.
         * @param duration The keepalive time duration (defaults to infinite/disabled if not set)
         * @note Supported on JVM/Android and Native platforms only.
         */
        fun keepAliveTime(duration: Duration): Builder

        /**
         * Sets the keepalive timeout, the timeout for keepalive ping requests.
         * @param duration The keepalive timeout duration (defaults to 20 seconds if not set)
         * @note Supported on JVM/Android and Native platforms only.
         */
        fun keepAliveTimeout(duration: Duration): Builder

        /**
         * Sets whether keepalive will be performed during idle periods and when there are no outstanding RPCs on a connection.
         * @param keepAliveWithoutCalls true if keepalive should be performed even when there are no calls (defaults to false if not set)
         * @note Supported on JVM/Android and Native platforms only.
         */
        fun keepAliveWithoutCalls(keepAliveWithoutCalls: Boolean): Builder

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
