package io.github.timortel.kotlin_multiplatform_grpc_lib.message

interface MessageDeserializer<T : JSImpl> {
    fun deserializeBinary(bytes: dynamic): T
}