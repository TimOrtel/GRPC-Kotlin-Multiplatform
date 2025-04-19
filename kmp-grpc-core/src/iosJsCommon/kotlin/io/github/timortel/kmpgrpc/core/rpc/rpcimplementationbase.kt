package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.IosJsChannel
import io.github.timortel.kmpgrpc.core.StatusException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

/**
 * Executes a gRPC call with a non-streaming response while handling channel shutdowns.
 */
@Throws(StatusException::class, CancellationException::class)
internal suspend fun <RESP> unaryResponseCallBaseImplementation(
    channel: IosJsChannel,
    performCall: suspend () -> RESP
): RESP {
    if (channel.isShutdown) throw StatusException.UnavailableDueToShutdown

    return coroutineScope {
        val waitForCancellationJob = launch {
            channel.isShutdownImmediately.first { it }

            Unit
        }

        val responseDeferred = async {
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
    channel: IosJsChannel,
    responseFlow: Flow<RESP>
): Flow<RESP> {
    return channelFlow {
        val emitJob = launch {
            if (channel.isShutdown) throw StatusException.UnavailableDueToShutdown

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
