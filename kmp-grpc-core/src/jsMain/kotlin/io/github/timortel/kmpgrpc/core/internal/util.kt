package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.KMCode
import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.KMStatus
import io.github.timortel.kmpgrpc.core.rpc.Status
import io.github.timortel.kmpgrpc.core.rpc.MethodType as JsMethodType

internal val String.kmMethodType: KMMethodDescriptor.MethodType
    get() = when (this) {
        JsMethodType.UNARY -> KMMethodDescriptor.MethodType.UNARY
        JsMethodType.SERVER_STREAMING -> KMMethodDescriptor.MethodType.SERVER_STREAMING
        JsMethodType.BIDI_STREAMING -> KMMethodDescriptor.MethodType.BIDI_STREAMING
        else -> throw IllegalArgumentException("Unknown method type $this")
    }

internal val Status.toKmStatus: KMStatus
    get() = KMStatus(
        code = KMCode.getCodeForValue(code.toInt()),
        statusMessage = details
    )


internal fun KMStatus.toJsStatus(metadata: KMMetadata): Status = Status(
    code = code.value,
    details = statusMessage,
    metadata = metadata.metadataMap
)
