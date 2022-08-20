package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import platform.Foundation.NSData

interface MessageDeserializer<T : KMMessage> {
    fun deserialize(`data`: NSData): T
}