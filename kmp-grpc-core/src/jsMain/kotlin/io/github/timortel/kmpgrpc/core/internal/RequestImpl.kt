package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallOptions
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.core.Request

internal data class RequestImpl(
    val requestMessage: dynamic,
    val methodDescriptor: MethodDescriptor,
    val metadata: dynamic,
    val callOptions: CallOptions
) : Request {
    override fun getRequestMessage(): dynamic = requestMessage

    override fun getMethodDescriptor(): MethodDescriptor = methodDescriptor

    override fun getMetadata(): dynamic = metadata

    override fun getCallOptions(): CallOptions = callOptions
}
