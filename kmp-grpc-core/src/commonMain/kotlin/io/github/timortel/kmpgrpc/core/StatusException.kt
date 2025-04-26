package io.github.timortel.kmpgrpc.core

import kotlin.time.Duration

/**
 * Represents an exception that includes a gRPC [Status] and an associated cause for the error.
 *
 * This exception is generally used to encapsulate detailed status information,
 * including a status code and message, along with the underlying cause of the error.
 *
 * @constructor Creates a new instance of the exception with the given [Status] and an optional cause.
 * @property status The [Status] associated with the error, which includes the error code and message.
 * @property cause The underlying throwable that caused this exception, or `null` if no cause is available.
 */
class StatusException internal constructor(val status: Status, override val cause: Throwable?) : RuntimeException(cause) {
    companion object {
        /**
         * Used by both iOS and JavaScript implementations.
         */
        val UnavailableDueToShutdown = StatusException(
            status = Status(code = Code.UNAVAILABLE, statusMessage = "The channel is shutdown."),
            cause = null
        )

        val CancelledDueToShutdown = StatusException(
            status = Status(code = Code.CANCELLED, statusMessage = "Call was cancelled due to channel shutdown."),
            cause = null
        )

        val InternalOnlyExpectedOneElement = StatusException(
            status = Status(code = Code.INTERNAL, statusMessage = "Expected call to only yield one element, but received more than 1."),
            cause = null
        )

        val InternalExpectedAtLeastOneElement = StatusException(
            status = Status(code = Code.INTERNAL, statusMessage = "Expected call to yield exactly one element, but received none."),
            cause = null
        )

        fun requestTimeout(duration: Duration, cause: Throwable?) = StatusException(
            status = Status(
                code = Code.DEADLINE_EXCEEDED,
                statusMessage = "RPC request timeout exceeded after set deadline of $duration"
            ),
            cause = cause
        )

        fun internal(message: String, cause: Throwable? = null) = StatusException(
            status = Status(
                code = Code.INTERNAL,
                statusMessage = message
            ),
            cause = cause
        )
    }
}
