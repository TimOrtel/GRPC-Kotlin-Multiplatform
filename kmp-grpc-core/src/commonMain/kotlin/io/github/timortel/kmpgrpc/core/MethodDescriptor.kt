package io.github.timortel.kmpgrpc.core

/**
 * Represents a descriptor for a gRPC method, containing its fully-qualified name and type.
 *
 * @property fullMethodName The fully qualified name of the gRPC method.
 * @property methodType The type of gRPC method, categorized as one of the values in [MethodType].
 */
data class MethodDescriptor(
    val fullMethodName: String,
    val methodType: MethodType
) {

    /**
     * Represents the type of gRPC method. Defines the interaction pattern between the client and server.
     */
    enum class MethodType {
        UNARY,
        SERVER_STREAMING,
        CLIENT_STREAMING,
        BIDI_STREAMING
    }
}
