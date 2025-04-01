package io.github.timortel.kmpgrpc.core.message

/**
 * Construct a the message of type [T] based on the data of type [T].
 */
interface MessageDeserializer<T : KMMessage, K> {
    fun deserialize(`data`: K): T
}