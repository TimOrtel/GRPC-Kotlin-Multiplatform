package io.github.timortel.kotlin_multiplatform_grpc_lib

/**
 * Represents a [grpc status](https://grpc.github.io/grpc/core/md_doc_statuscodes.html?ref=apisyouwonthate.com).
 */
data class KMStatus(val code: KMCode, val statusMessage: String)
