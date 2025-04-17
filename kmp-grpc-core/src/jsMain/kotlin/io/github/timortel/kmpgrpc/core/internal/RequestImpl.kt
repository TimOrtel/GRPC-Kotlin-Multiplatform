package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.JsMetadata
import io.github.timortel.kmpgrpc.core.external.CallOptions
import io.github.timortel.kmpgrpc.core.external.MethodDescriptor
import io.github.timortel.kmpgrpc.core.external.Request

internal data class RequestImpl(
    val requestMessage: dynamic,
    val methodDescriptor: MethodDescriptor,
    val metadata: JsMetadata,
    val callOptions: CallOptions
) : Request {
    override fun getRequestMessage(): dynamic = requestMessage

    override fun getMethodDescriptor(): MethodDescriptor = methodDescriptor

    override fun getMetadata(): JsMetadata = metadata

    override fun getCallOptions(): CallOptions = callOptions
}
