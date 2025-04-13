package io.github.timortel.kmpgrpc.core

/**
 * Metadata allows you to pass key-value pairs to your requests, for example authentication metadata.
 * Please refer to [the official metadata documentation](https://grpc.io/docs/what-is-grpc/core-concepts/#metadata).
 *
 * @property metadataMap the initial key-value pair configuration
 */
data class KMMetadata(val metadataMap: MutableMap<String, String> = mutableMapOf()) {

    constructor(metadataMap: Map<String, String>) : this(metadataMap.toMutableMap())

    /**
     * Add a new entry to the key-value pairs. Replaces the current entry if the key already exists.
     */
    operator fun set(key: String, value: String) {
        metadataMap[key] = value
    }

    /**
     * Return the value for the given key, or null if the key does not exist in the map.
     */
    operator fun get(key: String) = metadataMap[key]

    /**
     * Join two metadata maps.
     */
    operator fun plus(other: KMMetadata) = KMMetadata((metadataMap + other.metadataMap).toMutableMap())
}