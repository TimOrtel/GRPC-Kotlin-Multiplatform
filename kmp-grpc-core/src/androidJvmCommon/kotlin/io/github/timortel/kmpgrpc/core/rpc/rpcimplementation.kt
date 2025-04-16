package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.grpc.CallOptions
import io.grpc.MethodDescriptor
import io.grpc.StatusException
import io.grpc.kotlin.ClientCalls
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlin.jvm.Throws

@Throws(KMStatusException::class)
suspend fun <REQ, RESP> unaryRpc(
    channel: KMChannel,
    method: MethodDescriptor<REQ, RESP>,
    request: REQ,
    callOptions: CallOptions,
    headers: KMMetadata
): RESP {
    return try {
        ClientCalls.unaryRpc(
            channel = channel.channel,
            method = method,
            request = request,
            callOptions = callOptions,
            headers = headers.jvmMetadata
        )
    } catch (e: StatusException) {
        throw e.kmStatusException
    }
}

fun <REQ, RESP> serverStreamingRpc(
    channel: KMChannel,
    method: MethodDescriptor<REQ, RESP>,
    request: REQ,
    callOptions: CallOptions,
    headers: KMMetadata
): Flow<RESP> {
    return ClientCalls.serverStreamingRpc(
        channel = channel.channel,
        method = method,
        request = request,
        callOptions = callOptions,
        headers = headers.jvmMetadata
    )
        .catch { e ->
            if (e is StatusException) throw e.kmStatusException
            else throw e
        }
}

private val StatusException.kmStatusException: KMStatusException
    get() = KMStatusException(
        status = KMStatus(
            code = KMCode.getCodeForValue(status.code.value()),
            statusMessage = status.description.orEmpty()
        ),
        cause = this
    )
