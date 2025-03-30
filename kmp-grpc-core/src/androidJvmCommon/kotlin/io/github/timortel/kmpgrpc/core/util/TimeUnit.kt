package io.github.timortel.kmpgrpc.core.util

/**
 * On the jvm wrap [TimeUnit] with the java [java.util.concurrent.TimeUnit].
 */
actual enum class TimeUnit(val javaTimeUnit: java.util.concurrent.TimeUnit) {
    DAYS(java.util.concurrent.TimeUnit.DAYS),
    HOURS(java.util.concurrent.TimeUnit.HOURS),
    MILLISECONDS(java.util.concurrent.TimeUnit.MILLISECONDS),
    MINUTES(java.util.concurrent.TimeUnit.MINUTES),
    SECONDS(java.util.concurrent.TimeUnit.SECONDS)
}