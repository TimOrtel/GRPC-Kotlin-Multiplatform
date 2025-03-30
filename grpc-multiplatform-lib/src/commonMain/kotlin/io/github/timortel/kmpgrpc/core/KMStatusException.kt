package io.github.timortel.kmpgrpc.core

/**
 * An exception throws when an erroneous status occurs in a call.
 */
data class KMStatusException(val status: KMStatus, override val cause: Throwable?) : RuntimeException(cause)
