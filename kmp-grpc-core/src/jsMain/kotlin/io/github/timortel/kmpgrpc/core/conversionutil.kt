package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.external.MethodType
import io.github.timortel.kmpgrpc.core.external.Status
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import kotlin.js.json

typealias JsMetadata = io.github.timortel.kmpgrpc.core.external.Metadata

internal val Metadata.jsMetadata: JsMetadata
    get() = json(
        *entries.map { (key, value) -> key to value }.toTypedArray()
    ).unsafeCast<JsMetadata>()

internal val String.kmMethodType: KMMethodDescriptor.MethodType
    get() = when (this) {
        MethodType.UNARY -> KMMethodDescriptor.MethodType.UNARY
        MethodType.SERVER_STREAMING -> KMMethodDescriptor.MethodType.SERVER_STREAMING
        MethodType.BIDI_STREAMING -> KMMethodDescriptor.MethodType.BIDI_STREAMING
        else -> throw IllegalArgumentException("Unknown method type $this")
    }


internal fun KMStatus.toJsStatus(metadata: Metadata): Status = js("{}").unsafeCast<Status>().apply {
    code = this@toJsStatus.code.value
    details = this@toJsStatus.statusMessage
    this.metadata = metadata.entries
}

internal val ByteArray.native: Uint8Array get() {
    return Uint8Array(this.toTypedArray())
}

internal val Uint8Array.common: ByteArray get() {
    return Int8Array(this.buffer, this.byteOffset, this.length).unsafeCast<ByteArray>()
}