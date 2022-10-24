package io.github.timortel.kotlin_multiplatform_grpc_lib.message

interface MessageDeserializer<T : KMMessage, K> {
    fun deserialize(`data`: K): T
}