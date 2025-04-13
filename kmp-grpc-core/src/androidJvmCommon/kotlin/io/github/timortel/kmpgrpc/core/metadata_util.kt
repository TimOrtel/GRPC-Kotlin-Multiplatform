package io.github.timortel.kmpgrpc.core

/**
 * Convert the given multiplatform metadata to Jvm native [io.grpc.Metadata].
 */
val KMMetadata.jvmMetadata: io.grpc.Metadata
    get() = io.grpc.Metadata().apply {
        metadataMap.forEach { (key, value) ->
            val metadataKey = io.grpc.Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER)
            put(metadataKey, value)
        }
    }

val io.grpc.Metadata.kmMetadata: KMMetadata
    get() {
        return KMMetadata(
            metadataMap = keys().mapNotNull { keyName: String ->
                val key = io.grpc.Metadata.Key.of(keyName, io.grpc.Metadata.ASCII_STRING_MARSHALLER)

                val value = this@kmMetadata.get(key) ?: return@mapNotNull null
                keyName to value
            }
                .toMap()
                .toMutableMap()
        )
    }
