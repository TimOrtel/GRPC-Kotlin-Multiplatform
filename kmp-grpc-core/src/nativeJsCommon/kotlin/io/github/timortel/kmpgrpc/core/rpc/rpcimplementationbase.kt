package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.Code
import io.github.timortel.kmpgrpc.core.NativeJsChannel
import io.github.timortel.kmpgrpc.core.metadata.Metadata
import io.github.timortel.kmpgrpc.core.Status
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kmpgrpc.core.metadata.Key
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

private const val KEY_GRPC_STATUS = "grpc-status"
private const val KEY_GRPC_MESSAGE = "grpc-message"

private val grpcStatusKey = Key.AsciiKey(KEY_GRPC_STATUS)
private val grpcMessageKey = Key.AsciiKey(KEY_GRPC_MESSAGE)

/**
 * Executes a gRPC call with a non-streaming response while handling channel shutdowns.
 */
@Throws(StatusException::class, CancellationException::class)
internal suspend fun <RESP> unaryResponseCallBaseImplementation(
    channel: NativeJsChannel,
    performCall: suspend () -> RESP
): RESP {
    if (channel.isShutdown.value) throw StatusException.UnavailableDueToShutdown

    return coroutineScope {
        val waitForCancellationJob = launch {
            channel.isShutdownImmediately.first { it }

            Unit
        }

        val responseDeferred = async {
            if (channel.isShutdown.value) throw StatusException.UnavailableDueToShutdown

            performCall()
        }

        select {
            waitForCancellationJob.onJoin {
                responseDeferred.cancel("Cancelled due to channel shutdown")
                throw StatusException.CancelledDueToShutdown
            }

            responseDeferred.onAwait { result ->
                waitForCancellationJob.cancel()
                result
            }
        }
    }
}

/**
 * The flow throws [StatusException] with code [io.github.timortel.kmpgrpc.core.Code.UNAVAILABLE] on closed channels.
 * @return a flow that executes a server side streaming gRPC call operation while handling channel shutdowns.
 */
internal fun <RESP> streamingResponseCallBaseImplementation(
    channel: NativeJsChannel,
    responseFlow: Flow<RESP>
): Flow<RESP> {
    return channelFlow {
        val emitJob = launch {
            if (channel.isShutdown.value) throw StatusException.UnavailableDueToShutdown

            responseFlow.collect(::send)
        }

        val awaitShutdownJob = launch {
            channel.isShutdownImmediately.first { it }
        }

        select {
            emitJob.onJoin {
                awaitShutdownJob.cancel()
            }

            awaitShutdownJob.onJoin {
                emitJob.cancel()

                throw StatusException.CancelledDueToShutdown
            }
        }
    }
}

internal fun extractStatusFromMetadataAndVerify(metadata: Metadata) {
    return extractStatusFromMetadataAndVerify(metadata) { it }
}

internal fun extractStatusFromMetadataAndVerify(metadata: Metadata, runInterceptors: (Status) -> Status) {
    val status = extractStatusFromMetadata(metadata)
    if (status != null) {
        val finalStatus = runInterceptors(status)

        if (finalStatus.code != Code.OK) {
            throw StatusException(
                status = finalStatus,
                cause = null
            )
        }
    }
}

private fun extractStatusFromMetadata(metadata: Metadata): Status? {
    val rawStatus = metadata[grpcStatusKey]

    return if (rawStatus != null && rawStatus.toIntOrNull() != null) {
        val code = Code.getCodeForValue(rawStatus.toInt())
        Status(
            code = code,
            statusMessage = metadata[grpcMessageKey].orEmpty()
        )
    } else null
}
