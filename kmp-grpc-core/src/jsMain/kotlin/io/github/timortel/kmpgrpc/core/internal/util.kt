package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.KMStatus
import io.github.timortel.kmpgrpc.core.Status
import io.github.timortel.kmpgrpc.core.MethodType as JsMethodType

internal val String.kmMethodType: KMMethodDescriptor.MethodType
    get() = when (this) {
        JsMethodType.UNARY -> KMMethodDescriptor.MethodType.UNARY
        JsMethodType.SERVER_STREAMING -> KMMethodDescriptor.MethodType.SERVER_STREAMING
        JsMethodType.BIDI_STREAMING -> KMMethodDescriptor.MethodType.BIDI_STREAMING
        else -> throw IllegalArgumentException("Unknown method type $this")
    }


internal fun KMStatus.toJsStatus(metadata: KMMetadata): Status = js("{}").unsafeCast<Status>().apply {
    code = this@toJsStatus.code.value
    details = this@toJsStatus.statusMessage
    this.metadata = metadata.metadataMap
}
