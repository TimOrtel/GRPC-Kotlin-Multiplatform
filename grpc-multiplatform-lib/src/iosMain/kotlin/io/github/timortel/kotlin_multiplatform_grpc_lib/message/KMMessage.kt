package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import cocoapods.Protobuf.GPBCodedOutputStream
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
        serialize(stream)

        return data
    }

    fun serialize(stream: GPBCodedOutputStream)
}