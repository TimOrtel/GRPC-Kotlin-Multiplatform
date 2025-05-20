package io.github.timortel.kmpgrpc.core.metadata

/**
 * Key representation in [Metadata]. To create an instance of a `Key`, either [AsciiKey] or [BinaryKey] can be used.
 */
sealed interface Key<T> {

    companion object {
        internal const val BINARY_KEY_SUFFIX = "-bin"

        /**
         * Creates a Key instance based on the provided name. If the name ends with "-bin",
         * a BinaryKey is returned. Otherwise, an AsciiKey is returned.
         *
         * @param name The name of the key. Determines the type of key to be returned.
         * @return A Key instance, either BinaryKey or AsciiKey, based on the format of the name.
         */
        fun fromName(name: String): Key<*> {
            return if (name.endsWith(BINARY_KEY_SUFFIX)) {
                BinaryKey(name)
            } else {
                AsciiKey(name)
            }
        }
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
