package io.github.timortel.kmpgrpc.core.metadata

import kotlin.collections.emptyMap

/**
 * Metadata allows you to pass key-value pairs to your requests, for example, authentication metadata.
 * Instances of [Metadata] are immutable. A key may have multiple values associated with it.
 * Please refer to [the official metadata documentation](https://grpc.io/docs/what-is-grpc/core-concepts/#metadata).
 */
class Metadata private constructor(
    internal val asciiMap: MultiMap<Key.AsciiKey, String>,
    internal val binaryMap: MultiMap<Key.BinaryKey, ByteArray>
) {

    private constructor(asciiMap: Map<Key.AsciiKey, Set<String>>, binaryMap: Map<Key.BinaryKey, Set<ByteArray>>) :
            this(MultiMap(asciiMap), MultiMap(binaryMap))

    companion object {

        /**
         * Creates a `Metadata` instance from the provided vararg entries.
         *
         * @param entries the entries to be included in the metadata.
         * @return a new `Metadata` instance containing the provided entries.
         */
        fun of(vararg entries: Entry<*>): Metadata = of(entries.toList())

        /**
         * Constructs a `Metadata` instance from a list of key-value pairs.
         *
         * @param entries a list of key-value pairs.
         * @return a new `Metadata` instance containing the provided key-value pairs.
         */
        fun of(entries: List<Entry<*>>): Metadata {
            val asciiMap = mutableMapOf<Key.AsciiKey, Set<String>>()
            val binaryMap = mutableMapOf<Key.BinaryKey, Set<ByteArray>>()

            @Suppress("UNCHECKED_CAST")
            entries.groupBy { it.key }.forEach { (key, entries) ->
                when (key) {
                    is Key.AsciiKey -> {
                        asciiMap.put(key, entries.flatMap { it.values }.toSet() as Set<String>)
                    }

                    is Key.BinaryKey -> {
                        binaryMap.put(key, entries.flatMap { it.values }.toSet() as Set<ByteArray>)
                    }
                }
            }

            return Metadata(MultiMap(asciiMap), MultiMap(binaryMap))
        }

        /**
         * Creates a new `Metadata` instance with a single key-value pair.
         *
         * @param key the key for the metadata entry.
         * @param value the value for the metadata entry.
         * @return a new `Metadata` instance containing the specified key-value pair.
         */
        fun <T> of(key: Key<T>, value: T): Metadata {
            return when (key) {
                is Key.AsciiKey -> Metadata(mapOf(key to setOf(value as String)), emptyMap())
                is Key.BinaryKey -> Metadata(emptyMap(), mapOf(key to setOf(value as ByteArray)))
            }
        }

        /**
         * Creates an empty `Metadata` instance with no key-value pairs.
         *
         * @return a new `Metadata` instance containing no entries.
         */
        fun empty(): Metadata = Metadata(emptyMap(), emptyMap())
    }

    /**
     * A set of all keys present in the metadata.
     */
    val keys: Set<Key<*>> = asciiMap.keys + binaryMap.keys

    /**
     * A list of all entries in this metadata instance.
     */
    val entries: List<Entry<*>>
        get() = asciiMap.entries.map { Entry.Ascii(it.first, it.second) } +
                binaryMap.entries.map { Entry.Binary(it.first, it.second) }

    /**
     * Retrieves the last value associated with the specified key from the metadata.
     *
     * @param key the key whose associated value is to be retrieved.
     * @return the value associated with the specified key, or null if the key is not present in the metadata.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: Key<T>): T? = when (key) {
        is Key.AsciiKey -> asciiMap.getLast(key) as T?
        is Key.BinaryKey -> binaryMap.getLast(key) as T?
    }

    /**
     * Retrieves all values associated with the specified key from the metadata.
     *
     * @param key the key whose associated values are to be retrieved.
     * @return a set of values associated with the specified key, or an empty set if the key is not present.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getAll(key: Key<T>): Set<T> = when (key) {
        is Key.AsciiKey -> asciiMap.getAll(key) as Set<T>
        is Key.BinaryKey -> binaryMap.getAll(key) as Set<T>
    }

    /**
     * Combines the current `Metadata` instance with another `Metadata` instance.
     *
     * @param other the `Metadata` instance to be added.
     * @return a new `Metadata` instance containing the combined entries from both metadata instances.
     */
    operator fun plus(other: Metadata): Metadata = Metadata(asciiMap + other.asciiMap, binaryMap + other.binaryMap)

    operator fun <T> minus(key: Key<T>): Metadata = when (key) {
        is Key.AsciiKey -> Metadata(asciiMap - key, binaryMap)
        is Key.BinaryKey -> Metadata(asciiMap, binaryMap - key)
    }

    /**
     * Adds a new key-value pair to the existing metadata and returns a new `Metadata` instance with the updated entries.
     *
     * @param key the key of the entry to be added.
     * @param value the value of the entry to be added.
     * @return a new `Metadata` instance containing the additional key-value pair.
     */
    fun <T> withEntry(key: Key<T>, value: T): Metadata = when (key) {
        is Key.AsciiKey -> Metadata(asciiMap + (key to value as String), binaryMap)
        is Key.BinaryKey -> Metadata(asciiMap, binaryMap + (key to value as ByteArray))
    }

    /**
     * Creates a new `Metadata` instance by adding the provided key-value pair to the existing metadata.
     *
     * @param entry a key-value pair to be added to the metadata.
     * @return a new `Metadata` instance containing the additional key-value pair.
     */
    fun <T> withEntry(entry: Pair<Key<T>, T>): Metadata = withEntry(entry.first, entry.second)

    /**
     * Creates a new `Metadata` instance by appending the given list of key-value pairs to the existing metadata entries.
     *
     * @param entries a list of key-value pairs to be added to the metadata.
     * @return a new `Metadata` instance containing the combined key-value pairs from the current metadata and the provided list.
     */
    fun <T> withEntries(entries: List<Entry<*>>): Metadata = this + of(entries)
}
