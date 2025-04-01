@file:OptIn(ExperimentalTime::class)

package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun Timestamp.Companion.fromInstant(value: Instant): Timestamp = timestamp {
    seconds = value.epochSeconds
    nanos = value.nanosecondsOfSecond
}

fun Timestamp.toInstant(): Instant = Instant.fromEpochSeconds(seconds, nanos)

operator fun Timestamp.minus(other: Timestamp): Duration =
    Duration.fromDuration(toInstant() - other.toInstant())

operator fun Timestamp.plus(other: Duration): Timestamp =
    Timestamp.fromInstant(toInstant() + other.toDuration())
