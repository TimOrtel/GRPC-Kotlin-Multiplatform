package io.github.timortel.kmpgrpc.core.message

import cocoapods.Protobuf.GPBCodedOutputStream
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

    /**
     * Serializes this message and returns it as [NSData].
     */
    fun serialize(): NSData {
        val data = NSMutableData().apply { setLength(requiredSize.toULong()) }
        val stream = GPBCodedOutputStream(data)
        serialize(CodedOutputStream(stream))

        return data
    }

    /**
     * Serializes this message and writes it directly to [CodedOutputStream].
     */
    fun serialize(stream: CodedOutputStream)
}


val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
val requiredSizeMessage: (KMMessage) -> UInt = { message -> message.requiredSize.toUInt() }