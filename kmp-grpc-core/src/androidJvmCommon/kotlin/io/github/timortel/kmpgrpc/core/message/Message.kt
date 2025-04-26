package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.io.internal.CodedOutputStreamImpl
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

actual interface Message {

    actual val requiredSize: Int

    actual val fullName: String

    actual fun serialize(): ByteArray {
        val buffer = Buffer()
        serialize(CodedOutputStreamImpl(buffer))
        return buffer.readByteArray()
    }

    actual fun serialize(stream: CodedOutputStream)
}
