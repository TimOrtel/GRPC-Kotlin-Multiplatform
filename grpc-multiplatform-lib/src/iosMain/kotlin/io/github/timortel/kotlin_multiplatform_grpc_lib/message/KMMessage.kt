package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import cocoapods.Protobuf.GPBCodedOutputStream
import io.github.timortel.kotlin_multiplatform_grpc_lib.io.CodedOutputStream
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSStream
import platform.posix.size_t

/**
 * Base specification.
 */
actual interface KMMessage {

    val requiredSize: size_t

    fun serialize(): NSData {
        val data = NSMutableData().apply { setLength(requiredSize) }
        val stream = GPBCodedOutputStream(data)
        serialize(CodedOutputStream(stream))

        return data
    }

    fun serialize(stream: CodedOutputStream)
}


val serializeMessage: (KMMessage, CodedOutputStream) -> Unit = { message, stream -> message.serialize(stream) }
val requiredSizeMessage: (KMMessage) -> UInt = { message -> message.requiredSize.toUInt() }