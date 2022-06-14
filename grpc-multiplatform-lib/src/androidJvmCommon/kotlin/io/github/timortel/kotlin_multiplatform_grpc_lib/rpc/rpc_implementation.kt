package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMCode
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatus
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatusException
import io.grpc.StatusException

suspend fun <COMMON_RESPONSE> simpleCallImplementation(
    performCall: suspend () -> COMMON_RESPONSE
): COMMON_RESPONSE {
    return try {
        performCall()
    } catch (e: StatusException) {
        throw KMStatusException(KMStatus(KMCode.getCodeForValue(e.status.code.value()), e.status.description.orEmpty()), e)
    }
}