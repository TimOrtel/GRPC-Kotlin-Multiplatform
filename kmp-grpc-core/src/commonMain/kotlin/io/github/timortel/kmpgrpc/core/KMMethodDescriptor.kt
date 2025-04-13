package io.github.timortel.kmpgrpc.core

data class KMMethodDescriptor(
    val fullMethodName: String,
    val methodType: MethodType
) {
    enum class MethodType {
        UNARY,
        SERVER_STREAMING,
        CLIENT_STREAMING,
        BIDI_STREAMING
    }
}
