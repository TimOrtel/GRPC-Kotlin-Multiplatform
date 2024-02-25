package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMCode
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatus
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatusException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <JS_RESPONSE> simpleCallImplementation(
    performCall: (callback: (error: dynamic, response: JS_RESPONSE) -> Unit) -> Unit
): JS_RESPONSE {
    return suspendCoroutine { continuation ->
        performCall { error, response ->
            if (error == null) {
                continuation.resume(response)
            } else {
                continuation.resumeWithException(
                    KMStatusException(
                        KMStatus(
                            KMCode.getCodeForValue(error.code as Int),
                            (error.message as String?).orEmpty()
                        ), null
                    )
                )
            }
        }
    }
}

fun <JS_RESPONSE> serverSideStreamingCallImplementation(performCall: () -> dynamic): Flow<JS_RESPONSE> {
    return callbackFlow {
        val stream = performCall()
        stream.on("data") { data ->
            trySend(data as JS_RESPONSE)
        }

        stream.on("end") {
            close()
            Unit
        }

        stream.on("status") { status ->
            //If the status is not ok, we throw an error
            if (status.code as Int != 0) {
                close(
                    KMStatusException(
                        KMStatus(KMCode.getCodeForValue(status.code as Int), status.details as String),
                        null
                    )
                )
            }
        }

        stream.on("error") {
            close(KMStatusException(KMStatus(KMCode.UNKNOWN, "Unknown streaming error"), null))
        }

        awaitClose {
            stream.cancel() as Unit
        }
    }
        // Catch a very weird bug that occurs when calling close()
        .catch { if (it !is ClassCastException) throw it }
}
