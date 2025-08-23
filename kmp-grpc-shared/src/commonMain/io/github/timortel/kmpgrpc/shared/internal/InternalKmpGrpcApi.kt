package io.github.timortel.kmpgrpc.shared.internal

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is internal to kmp-grpc. " +
            "It must not be used outside of the package `io.github.timortel.kmpgrpc` " +
            "or outside of source code generated from proto definitions."
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalKmpGrpcApi
