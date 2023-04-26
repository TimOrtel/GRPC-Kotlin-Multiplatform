package io.github.timortel.kotlin_multiplatform_grpc_lib

/**
 * An exception throws when an erroneous status occurs in a call.
 */
class KMStatusException(val status: KMStatus, cause: Throwable?) : RuntimeException(cause)
