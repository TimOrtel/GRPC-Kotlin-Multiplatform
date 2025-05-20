package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.metadata.Metadata
import io.grpc.CallOptions
import io.grpc.MethodDescriptor
import io.grpc.StatusException
import io.grpc.kotlin.ClientCalls
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlin.jvm.Throws

@Throws(StatusException::class)
suspend fun <REQ, RESP> unaryRpc(
    channel: Channel,
    method: MethodDescriptor<REQ, RESP>,
    request: REQ,
    callOptions: CallOptions,
    headers: Metadata
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
        throw e.statusException
    }
}

fun <REQ, RESP> serverStreamingRpc(
    channel: Channel,
    method: MethodDescriptor<REQ, RESP>,
    request: REQ,
    callOptions: CallOptions,
    headers: Metadata
): Flow<RESP> {
    return ClientCalls.serverStreamingRpc(
        channel = channel.channel,
        method = method,
        request = request,
        callOptions = callOptions,
        headers = headers.jvmMetadata
    )
        .catch { e ->
            if (e is StatusException) throw e.statusException
            else throw e
        }
}

@Throws(StatusException::class)
suspend fun <REQ, RESP> clientStreamingRpc(
    channel: Channel,
    method: MethodDescriptor<REQ, RESP>,
    requests: Flow<REQ>,
    callOptions: CallOptions,
    headers: Metadata
): RESP {
    return try {
        ClientCalls.clientStreamingRpc(
            channel.channel,
            method = method,
            requests = requests,
            callOptions = callOptions,
            headers = headers.jvmMetadata
        )
    } catch (e: StatusException) {
        throw e.statusException
    }
}

@Throws(StatusException::class)
fun <REQ, RESP> bidiStreamingRpc(
    channel: Channel,
    method: MethodDescriptor<REQ, RESP>,
    requests: Flow<REQ>,
    callOptions: CallOptions,
    headers: Metadata
): Flow<RESP> {
    return ClientCalls.bidiStreamingRpc(
        channel.channel,
        method = method,
        requests = requests,
        callOptions = callOptions,
        headers = headers.jvmMetadata
    )
        .catch { e ->
            if (e is StatusException) throw e.statusException
            else throw e
        }
}


private val StatusException.statusException: io.github.timortel.kmpgrpc.core.StatusException
    get() = StatusException(
        status = Status(
            code = Code.getCodeForValue(status.code.value()),
            statusMessage = status.description.orEmpty()
        ),
        cause = this
    )
