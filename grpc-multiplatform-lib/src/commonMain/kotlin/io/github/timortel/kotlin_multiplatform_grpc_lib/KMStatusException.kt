package io.github.timortel.kotlin_multiplatform_grpc_lib

class KMStatusException(val status: KMStatus, cause: Throwable?) : RuntimeException(cause) {
}