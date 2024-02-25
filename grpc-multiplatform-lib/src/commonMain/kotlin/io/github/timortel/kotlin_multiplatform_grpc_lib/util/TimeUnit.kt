package io.github.timortel.kotlin_multiplatform_grpc_lib.util

/**
 * Multiplatform time unit specification.
 */
expect enum class TimeUnit {
    DAYS,
    HOURS,
    MILLISECONDS,
    MINUTES,
    SECONDS
}