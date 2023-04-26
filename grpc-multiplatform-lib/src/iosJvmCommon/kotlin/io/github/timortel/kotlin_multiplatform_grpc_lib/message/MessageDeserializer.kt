package io.github.timortel.kotlin_multiplatform_grpc_lib.message

/**
 * Construct a the message of type [T] based on the data of type [T].
 */
interface MessageDeserializer<T : KMMessage, K> {
    fun deserialize(`data`: K): T
}
