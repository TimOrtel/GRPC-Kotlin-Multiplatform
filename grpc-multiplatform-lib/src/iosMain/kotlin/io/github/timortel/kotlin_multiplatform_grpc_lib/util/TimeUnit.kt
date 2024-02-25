package io.github.timortel.kotlin_multiplatform_grpc_lib.util

/**
 * Implements the time unit using a factor that converts the time unit into milliseconds.
 */
actual enum class TimeUnit(val toMilliFactor: Long) {
    DAYS(24 * 60 * 60 * 1000),
    HOURS(60 * 60 * 1000),
    MILLISECONDS(1),
    MINUTES(60 * 1000),
    SECONDS(1000)
}