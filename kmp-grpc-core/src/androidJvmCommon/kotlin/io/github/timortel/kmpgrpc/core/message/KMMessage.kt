package io.github.timortel.kmpgrpc.core.message

import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import io.github.timortel.kmpgrpc.core.io.CodedOutputStream
import io.github.timortel.kmpgrpc.core.io.JvmCodedOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * On the jvm, we wrap [com.google.protobuf.CodedOutputStream] to serialize our messages.
 */
actual interface KMMessage : MessageLite {

    /**
     * The size of this message when serialized in bytes.
     */
    val requiredSize: Int

    actual val fullName: String

    /**
     * Serializes this message and returns it as a [ByteArray].
     */
    actual fun serialize(): ByteArray {
        val data = ByteArray(requiredSize)
        val stream = com.google.protobuf.CodedOutputStream.newInstance(ByteBuffer.wrap(data))
        serialize(JvmCodedOutputStream(stream))

        return data
    }

    /**
     * Serializes this message and writes it directly to the [JvmCodedOutputStream].
     */
    actual fun serialize(stream: CodedOutputStream)

    override fun writeTo(output: OutputStream) {
        val bufferSize = requiredSize
        val codedOutput = com.google.protobuf.CodedOutputStream.newInstance(output, bufferSize)
        writeTo(codedOutput)
        codedOutput.flush()
    }

    override fun writeTo(output: com.google.protobuf.CodedOutputStream) {
        serialize(JvmCodedOutputStream(output))
    }

    override fun getSerializedSize(): Int = requiredSize

    override fun toByteString(): ByteString = ByteString.copyFrom(toByteArray())

    override fun toByteArray(): ByteArray = serialize()

    override fun writeDelimitedTo(output: OutputStream) {
        val stream = com.google.protobuf.CodedOutputStream.newInstance(output)
        stream.writeUInt32NoTag(requiredSize)
        writeTo(stream)
    }

    override fun newBuilderForType(): MessageLite.Builder = throw NotImplementedError()

    override fun toBuilder(): MessageLite.Builder = throw NotImplementedError()

    override fun isInitialized(): Boolean = true
}
