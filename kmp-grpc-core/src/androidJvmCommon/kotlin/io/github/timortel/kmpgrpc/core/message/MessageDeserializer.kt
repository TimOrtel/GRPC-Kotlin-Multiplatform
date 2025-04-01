package io.github.timortel.kmpgrpc.core.message

actual interface MessageDeserializer<T : KMMessage> {
    actual fun deserialize(`data`: ByteArray): T {
        throw NotImplementedError()
    }
}
