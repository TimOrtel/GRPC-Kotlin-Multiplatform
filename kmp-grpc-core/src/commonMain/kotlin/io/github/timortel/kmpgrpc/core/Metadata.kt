package io.github.timortel.kmpgrpc.core

/**
 * Metadata allows you to pass key-value pairs to your requests, for example, authentication metadata.
 * Please refer to [the official metadata documentation](https://grpc.io/docs/what-is-grpc/core-concepts/#metadata).
 */
class Metadata private constructor(val entries: Map<String, String>) {

    companion object {

        /**
         * Creates a new `Metadata` instance using the provided map of key-value pairs.
         *
         * @param map a map containing key-value pairs to initialize the `Metadata` instance with.
         * @return a new `Metadata` instance containing the specified key-value pairs.
         */
        fun of(map: Map<String, String>): Metadata {
            return Metadata(map)
        }

        /**
         * Creates a new `Metadata` instance with a single key-value pair.
         *
         * @param key the key for the metadata entry.
         * @param value the value for the metadata entry.
         * @return a new `Metadata` instance containing the specified key-value pair.
         */
        fun of(key: String, value: String): Metadata = of(mapOf(Pair(key, value)))

        /**
         * Creates an empty `Metadata` instance with no key-value pairs.
         *
         * @return a new `Metadata` instance containing no entries.
         */
        fun empty(): Metadata = of(emptyMap())
    }

    /**
     * Retrieves the value associated with the specified key within the metadata.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value corresponding to the given key, or `null` if the key does not exist.
     */
    operator fun get(key: String): String? = entries[key]

    /**
     * Combines the current `Metadata` instance with another `Metadata` instance, merging their key-value pairs.
     * If there are duplicate keys, the values from the provided [other] metadata will overwrite those in the current metadata.
     *
     * @param other the `Metadata` instance to be combined with the current instance.
     * @return a new `Metadata` instance containing the merged key-value pairs.
     */
    operator fun plus(other: Metadata): Metadata = of(entries + other.entries)

    /**
     * Adds a new key-value pair to the existing metadata and returns a new `Metadata` instance with the updated entries.
     *
     * @param key the key of the entry to be added.
     * @param value the value of the entry to be added.
     * @return a new `Metadata` instance containing the additional key-value pair.
     */
    fun withEntry(key: String, value: String): Metadata = withEntry(Pair(key, value))

    /**
     * Creates a new `Metadata` instance by adding the provided key-value pair to the existing metadata.
     *
     * @param entry a key-value pair to be added to the metadata.
     * @return a new `Metadata` instance containing the additional key-value pair.
     */
    fun withEntry(entry: Pair<String, String>): Metadata = of(entries + entry)

    /**
     * Creates a new `Metadata` instance by appending the given list of key-value pairs to the existing metadata entries.
     *
     * @param entries a list of key-value pairs to be added to the metadata.
     * @return a new `Metadata` instance containing the combined key-value pairs from the current metadata and the provided list.
     */
    fun withEntries(entries: List<Pair<String, String>>): Metadata = of(this.entries + entries)

    /**
     * Creates a new `Metadata` instance by adding the provided map of key-value pairs to the existing metadata.
     *
     * @param entries a map containing key-value pairs to be added to the metadata.
     * @return a new `Metadata` instance containing the combined key-value pairs from the current metadata and the provided map.
     */
    fun withEntries(entries: Map<String, String>): Metadata = of(this.entries + entries)
}
