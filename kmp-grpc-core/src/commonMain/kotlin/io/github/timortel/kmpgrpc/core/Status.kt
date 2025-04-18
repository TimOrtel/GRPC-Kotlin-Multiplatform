package io.github.timortel.kmpgrpc.core

/**
 * Represents a [grpc status](https://grpc.github.io/grpc/core/md_doc_statuscodes.html?ref=apisyouwonthate.com).
 */
data class Status(val code: Code, val statusMessage: String)
