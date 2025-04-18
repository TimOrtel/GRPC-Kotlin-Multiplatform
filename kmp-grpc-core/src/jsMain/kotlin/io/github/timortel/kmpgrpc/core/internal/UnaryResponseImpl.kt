package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.JsMetadata
import io.github.timortel.kmpgrpc.core.external.Metadata
import io.github.timortel.kmpgrpc.core.external.MethodDescriptor
import io.github.timortel.kmpgrpc.core.external.Status
import io.github.timortel.kmpgrpc.core.external.UnaryResponse

internal class UnaryResponseImpl<RESP>(
    val resp: RESP,
    val metadata: Metadata,
    val status: Status,
    val methodDescriptor: MethodDescriptor
) : UnaryResponse<RESP> {
    override fun getResponseMessage(): RESP = resp

    override fun getMetadata(): JsMetadata = metadata

    override fun getStatus(): Status = status

    override fun getMethodDescriptor(): MethodDescriptor = methodDescriptor
}
