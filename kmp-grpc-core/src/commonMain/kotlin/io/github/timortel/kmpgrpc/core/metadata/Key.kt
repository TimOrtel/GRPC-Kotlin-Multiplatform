package io.github.timortel.kmpgrpc.core.metadata

/**
 * Key representation in [Metadata].
 */
sealed interface Key<T> {

    companion object {
        internal const val BINARY_KEY_SUFFIX = "-bin"
    }

    val name: String

    /**
     * Represents a key used for storing ASCII-based values in metadata.
     *
     * @property name The name of the ASCII key, which serves as its unique identifier in metadata. Must not end in "-bin".
     */
    data class AsciiKey(override val name: String) : Key<String> {
        init {
            if (name.endsWith(BINARY_KEY_SUFFIX)) {
                throw IllegalArgumentException("name must not end with $BINARY_KEY_SUFFIX")
            }
        }
    }

    /**
     * Represents a key used for storing binary-based values in metadata.
     *
     * @property name The name of the binary key, which serves as its unique identifier in metadata. Must end in "-bin".
     */
    data class BinaryKey(override val name: String) : Key<ByteArray> {
        init {
            if (!name.endsWith(BINARY_KEY_SUFFIX)) {
                throw IllegalArgumentException("name must end with $BINARY_KEY_SUFFIX")
            }
        }
    }
}
