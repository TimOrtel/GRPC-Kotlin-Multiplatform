package io.github.timortel.kmpgrpc.core.message

/**
 * Construct a message of type [T].
 */
expect interface MessageDeserializer<T : KMMessage> {
    open fun deserialize(`data`: ByteArray): T
}
