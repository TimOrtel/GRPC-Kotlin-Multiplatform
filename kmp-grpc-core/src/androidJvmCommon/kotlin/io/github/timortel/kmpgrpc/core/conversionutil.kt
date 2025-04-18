package io.github.timortel.kmpgrpc.core

import io.grpc.Status

typealias JvmMetadata = io.grpc.Metadata

/**
 * Convert the given multiplatform metadata to Jvm native [JvmMetadata].
 */
internal val Metadata.jvmMetadata: JvmMetadata
    get() = JvmMetadata().apply {
        entries.forEach { (key, value) ->
            val metadataKey = io.grpc.Metadata.Key.of(key, JvmMetadata.ASCII_STRING_MARSHALLER)
            put(metadataKey, value)
        }
    }

internal val JvmMetadata.kmMetadata: Metadata
    get() {
        return Metadata.of(
            keys().mapNotNull { keyName: String ->
                val key = io.grpc.Metadata.Key.of(keyName, JvmMetadata.ASCII_STRING_MARSHALLER)

                val value = this@kmMetadata.get(key) ?: return@mapNotNull null
                keyName to value
            }
                .toMap()
        )
    }

internal val io.github.timortel.kmpgrpc.core.Status.jvmStatus: Status
    get() = Status.fromCodeValue(code.value).withDescription(statusMessage)
