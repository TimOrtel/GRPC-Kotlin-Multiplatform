package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.metadata.Metadata
import kotlin.time.Duration

/**
 * Represents options for configuring a gRPC call.
 *
 * @property metadata Key-value pairs that can be used to send additional information with the request.
 * @property deadlineAfter The duration after which the call times out if not completed.
 */
data class CallOptions(val metadata: Metadata = Metadata.empty(), val deadlineAfter: Duration? = null) {
    operator fun plus(other: Metadata): CallOptions = copy(
        metadata = metadata + other
    )
}
