package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.KMCode
import io.github.timortel.kmpgrpc.core.KMStatus
import io.github.timortel.kmpgrpc.core.KMStatusException
import io.github.timortel.kmpgrpc.core.RpcError
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlin.js.Promise

suspend fun <JS_RESPONSE> simpleCallImplementation(
    performCall: () -> Promise<JS_RESPONSE>
): JS_RESPONSE {
    return try {
        performCall().await()
    } catch (e: RpcError) {
        throw KMStatusException(
            status = KMStatus(
                code = KMCode.getCodeForValue(e.code.toInt()),
                statusMessage = e.message
            ),
            cause = e
        )
    } catch (e: Exception) {
        throw e
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
        // Catch a weird bug that occurs when calling close()
        .catch { if (it !is ClassCastException) throw it }
}
