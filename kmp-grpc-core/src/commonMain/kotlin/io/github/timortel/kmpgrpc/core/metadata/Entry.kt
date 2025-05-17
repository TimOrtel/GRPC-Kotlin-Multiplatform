package io.github.timortel.kmpgrpc.core.metadata

/**
 * Represents an entry in metadata, which associates a key with a set of values.
 *
 * @param T The type of the values associated with this entry.
 */
sealed interface Entry<T> {

    val key: Key<T>
    val values: Set<T>

    data class Ascii(override val key: Key.AsciiKey, override val values: Set<String>) : Entry<String>

    data class Binary(override val key: Key.BinaryKey, override val values: Set<ByteArray>) : Entry<ByteArray>
}
