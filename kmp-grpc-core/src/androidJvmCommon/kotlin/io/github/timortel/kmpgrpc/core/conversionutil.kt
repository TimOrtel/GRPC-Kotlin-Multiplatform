package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.metadata.Entry
import io.github.timortel.kmpgrpc.core.metadata.Key
import io.github.timortel.kmpgrpc.core.metadata.Key.Companion.BINARY_KEY_SUFFIX
import io.github.timortel.kmpgrpc.core.metadata.Metadata
import io.grpc.Status

typealias JvmMetadata = io.grpc.Metadata

/**
 * Convert the given multiplatform metadata to Jvm native [JvmMetadata].
 */
internal val Metadata.jvmMetadata: JvmMetadata
    get() = JvmMetadata().apply {
        entries.forEach { entry ->
            when (entry) {
                is Entry.Ascii -> {
                    val metadataKey = io.grpc.Metadata.Key.of(entry.key.name, JvmMetadata.ASCII_STRING_MARSHALLER)
                    entry.values.forEach { put(metadataKey, it) }
                }

                is Entry.Binary -> {
                    val metadataKey = io.grpc.Metadata.Key.of(entry.key.name, JvmMetadata.BINARY_BYTE_MARSHALLER)
                    entry.values.forEach { put(metadataKey, it) }
                }
            }
        }
    }

internal val JvmMetadata.kmMetadata: Metadata
    get() {
        val entries: List<Entry<*>> = keys().mapNotNull { keyName ->
            if (keyName.endsWith(BINARY_KEY_SUFFIX)) {
                val key = io.grpc.Metadata.Key.of(keyName, JvmMetadata.BINARY_BYTE_MARSHALLER)

                val values = this@kmMetadata.getAll(key)?.toSet().orEmpty()
                Entry.Binary(Key.BinaryKey(keyName), values)
            } else {
                val key = io.grpc.Metadata.Key.of(keyName, JvmMetadata.ASCII_STRING_MARSHALLER)

                val value = this@kmMetadata.getAll(key)?.toSet().orEmpty()
                Entry.Ascii(Key.AsciiKey(keyName), value)
            }
        }

        return Metadata.of(entries)
    }

internal val io.github.timortel.kmpgrpc.core.Status.jvmStatus: Status
    get() = Status.fromCodeValue(code.value).withDescription(statusMessage)
