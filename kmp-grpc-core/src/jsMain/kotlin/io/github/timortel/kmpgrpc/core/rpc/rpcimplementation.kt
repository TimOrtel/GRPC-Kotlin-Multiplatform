package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.JsMetadata
import io.github.timortel.kmpgrpc.core.KMCode
import io.github.timortel.kmpgrpc.core.KMStatus
import io.github.timortel.kmpgrpc.core.KMStatusException
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.external.RpcError
import io.github.timortel.kmpgrpc.core.jsMetadata
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlin.js.Promise

/**
 * Executes the unary given call and maps [RpcError]s to [KMStatusException]s.
 *
 * @param performCall A suspendable lambda function that returns a Promise of the generic type JS_RESPONSE.
 * @param metadata The metadata that should be sent with this call.
 * @return The result of the call
 * @throws KMStatusException if an RpcError is caught, wrapping the error details into a KMStatusException.
 * @throws Exception if any other exception is caught during execution.
 */
suspend fun <JS_RESPONSE> unaryCallImplementation(
    metadata: Metadata,
    performCall: (metadata: JsMetadata) -> Promise<JS_RESPONSE>
): JS_RESPONSE {
    return try {
        performCall(metadata.jsMetadata).await()
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

/**
 * Handles server-side streaming calls by converting a dynamic call response into a Flow of type JS_RESPONSE.
 *
 * @param JS_RESPONSE The type of data expected in the streaming response.
 * @param metadata The metadata that should be sent with this call.
 * @param performCall A lambda function that performs the backend call and returns a dynamic streaming object.
 * @return A [Flow] instance emitting responses of type JS_RESPONSE, or errors if the streaming call fails.
 */
fun <JS_RESPONSE> serverSideStreamingCallImplementation(metadata: Metadata, performCall: (metadata: JsMetadata) -> dynamic): Flow<JS_RESPONSE> {
    return callbackFlow {
        val stream = performCall(metadata.jsMetadata)
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
