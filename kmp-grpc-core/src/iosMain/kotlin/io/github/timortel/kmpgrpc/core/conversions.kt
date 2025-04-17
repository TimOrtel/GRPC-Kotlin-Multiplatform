package io.github.timortel.kmpgrpc.core

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

internal val ByteArray.native: NSData
    get() {
        return memScoped {
            @OptIn(BetaInteropApi::class)
            NSData.create(bytes = allocArrayOf(this@native), length = this@native.size.toULong())
        }
    }

internal val NSData.common: ByteArray
    get() {
        if (length == 0uL) return byteArrayOf()

        val byteArray = ByteArray(length.toInt())
        byteArray.usePinned { memcpy(it.addressOf(0), bytes, length) }
        return byteArray
    }
