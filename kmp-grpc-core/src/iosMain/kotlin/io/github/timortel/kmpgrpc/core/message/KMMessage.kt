package io.github.timortel.kmpgrpc.core.message

import cocoapods.Protobuf.GPBCodedOutputStream
import io.github.timortel.kmpgrpc.core.io.IosCodedOutputStream
import io.github.timortel.kmpgrpc.core.common
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import platform.Foundation.NSData
import platform.Foundation.NSMutableData

/**
 * Base specification.
 */
actual interface KMMessage {

    /**
     * The size this message takes up in a byte array.
     */
    val requiredSize: Int

    actual val fullName: String

    /**
     * Serializes this message and returns it as [NSData].
     */
    fun serializeNative(): NSData {
        val data = NSMutableData().apply { setLength(requiredSize.toULong()) }
        val stream = GPBCodedOutputStream(data)
        serialize(IosCodedOutputStream(stream))

        return data
    }

    actual fun serialize(): ByteArray {
        return serializeNative().common
    }

    /**
     * Serializes this message and writes it directly to [IosCodedOutputStream].
     */
    actual fun serialize(stream: CodedOutputStream)
}
