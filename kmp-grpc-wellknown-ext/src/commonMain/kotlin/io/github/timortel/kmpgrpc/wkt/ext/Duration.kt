package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.Duration
import com.google.protobuf.duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private const val NANOS_PER_SECOND = 1_000_000_000L

fun Duration.Companion.ofSeconds(value: Long): Duration = duration {
    seconds = value
    nanos = 0
}

fun Duration.Companion.ofMillis(value: Long): Duration = Duration.fromDuration(value.milliseconds)

fun Duration.Companion.fromDuration(value: kotlin.time.Duration): Duration = duration {
    seconds = value.inWholeSeconds
    nanos = (value.inWholeNanoseconds - value.inWholeSeconds * NANOS_PER_SECOND).toInt()
}

fun Duration.toDuration(): kotlin.time.Duration {
    return seconds.seconds + nanos.nanoseconds
}

operator fun Duration.plus(other: Duration): Duration =
    Duration.fromDuration(toDuration() + other.toDuration())

operator fun Duration.minus(other: Duration): Duration =
    Duration.fromDuration(toDuration() - other.toDuration())

operator fun Duration.unaryMinus(): Duration = Duration.fromDuration(-toDuration())
