package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.io.internal.CodedOutputStreamImpl
import io.github.timortel.kmpgrpc.core.native
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import platform.Foundation.NSData

actual interface Message {

    actual val fullName: String

    actual val requiredSize: Int

    /**
     * Serializes this message and returns it as [NSData].
     */
    fun serializeNative(): NSData {
        return serialize().native
    }

    actual fun serialize(): ByteArray {
        val buffer = Buffer()
        serialize(CodedOutputStreamImpl(buffer))
        return buffer.readByteArray()
    }

    actual fun serialize(stream: CodedOutputStream)
}
