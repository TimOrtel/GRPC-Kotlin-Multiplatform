package io.github.timortel.kmpgrpc.core.util

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

val ByteArray.native: Uint8Array get() {
    return Uint8Array(this.toTypedArray())
}

val Uint8Array.common: ByteArray get() {
    return Int8Array(this.buffer, this.byteOffset, this.length).unsafeCast<ByteArray>()
}
