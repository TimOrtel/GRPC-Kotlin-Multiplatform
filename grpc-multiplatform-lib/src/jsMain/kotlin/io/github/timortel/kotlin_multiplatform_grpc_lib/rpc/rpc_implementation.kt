package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMCode
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatus
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMStatusException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
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

suspend fun <JS_RESPONSE> serverSideStreamingCallImplementation(performCall: () -> dynamic): Flow<JS_RESPONSE> {
    val flow = MutableSharedFlow<JS_RESPONSE>()
    val isDone = MutableStateFlow(false)

    val scope = CoroutineScope(coroutineContext)

    val stream = performCall()
    stream.on("data") { data ->
        @Suppress("UNCHECKED_CAST")
        data as JS_RESPONSE

        scope.launch {
            flow.emit(data)
        }
    }

    stream.on("end") {
        isDone.value = true
        Unit
    }

    stream.on("status") { status ->
        //If the status is not ok, we throw an error
        if (status.code as Int != 0) {
            throw KMStatusException(KMStatus(KMCode.getCodeForValue(status.code as Int), status.details as String), null)
        }
    }

    stream.on("error") {
        throw KMStatusException(KMStatus(KMCode.UNKNOWN, "Unknown streaming error"), null)
    }

    return isDone.takeWhile { !it }.transform { emitAll(flow) }.onCompletion {
        try {
            scope.cancel()
        } catch (_: IllegalStateException) {
        }
    }
}