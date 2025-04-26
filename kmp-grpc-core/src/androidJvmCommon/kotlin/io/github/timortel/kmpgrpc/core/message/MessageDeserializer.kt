package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.io.internal.CodedInputStreamImpl
import io.grpc.MethodDescriptor
import kotlinx.io.Buffer
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream
import java.io.InputStream

actual interface MessageDeserializer<T : Message> : MethodDescriptor.Marshaller<T> {

    actual fun deserialize(`data`: ByteArray): T {
        val buffer = Buffer()
        buffer.write(data)

        val stream = CodedInputStreamImpl(buffer)
        return deserialize(stream)
    }

    actual fun deserialize(stream: CodedInputStream): T

    override fun stream(value: T): InputStream {
        return ByteArrayInputStream(value.serialize())
    }

    override fun parse(stream: InputStream): T {
        return deserialize(CodedInputStreamImpl(stream.asSource().buffered()))
    }
}
