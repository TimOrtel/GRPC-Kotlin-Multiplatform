package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.core.Status
import io.github.timortel.kmpgrpc.core.UnaryResponse

class UnaryResponseImpl<RESP>(
    val resp: RESP,
    val metadata: Metadata,
    val status: Status,
    val methodDescriptor: MethodDescriptor
) : UnaryResponse<RESP> {
    override fun getResponseMessage(): RESP = resp

    override fun getMetadata(): Metadata = metadata

    override fun getStatus(): Status = status

    override fun getMethodDescriptor(): MethodDescriptor = methodDescriptor
}
