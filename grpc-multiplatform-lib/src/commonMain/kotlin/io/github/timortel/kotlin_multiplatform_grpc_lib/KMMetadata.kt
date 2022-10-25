package io.github.timortel.kotlin_multiplatform_grpc_lib

/**
 * Metadata wrapper.
 */
data class KMMetadata(val metadataMap: MutableMap<String, String> = mutableMapOf()) {

    operator fun set(key: String, value: String) {
        metadataMap[key] = value
    }

    operator fun get(key: String) = metadataMap[key]

    operator fun plus(other: KMMetadata) = KMMetadata((metadataMap + other.metadataMap).toMutableMap())
}