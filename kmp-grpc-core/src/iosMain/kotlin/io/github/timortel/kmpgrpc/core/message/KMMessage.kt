package io.github.timortel.kmpgrpc.core.message

import cocoapods.Protobuf.GPBCodedOutputStream
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.util.common
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
        serialize(CodedOutputStream(stream))

        return data
    }

    actual fun serialize(): ByteArray {
        return serializeNative().common
    }

    /**
     * Serializes this message and writes it directly to [CodedOutputStream].
     */
    fun serialize(stream: CodedOutputStream)
}


val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
val requiredSizeMessage: (KMMessage) -> UInt = { message -> message.requiredSize.toUInt() }