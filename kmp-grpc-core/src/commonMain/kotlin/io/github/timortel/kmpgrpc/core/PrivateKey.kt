package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.util.bytesAsPemContent
import io.github.timortel.kmpgrpc.core.util.parseBytesFromPemContent

/**
 * Represents a private key provided either in DER or PEM format.
 */
class PrivateKey internal constructor(internal val data: ByteArray) {

    internal val asPem: String get() = bytesAsPemContent(data, PEM_CONTAINER)

    companion object {
        private const val PEM_CONTAINER = "PRIVATE KEY"

        /**
         * Creates a [PrivateKey] instance from the given PEM-formatted text.
         *
         * This method validates that the input string contains a single private key
         * enclosed in standard PEM markers (i.e., `-----BEGIN PRIVATE KEY-----` and
         * `-----END PRIVATE KEY-----`). The enclosed Base64-encoded content is
         * decoded into DER bytes and returned as a [PrivateKey] instance.
         *
         * @param content The PEM-encoded private key string.
         * @return A parsed [PrivateKey] instance.
         *
         * @throws IllegalArgumentException If the input does not contain a valid
         * PEM-encoded private key.
         */
        fun fromPem(content: String): PrivateKey {
            return PrivateKey(parseBytesFromPemContent(content, PEM_CONTAINER))
        }

        /**
         * Creates a [PrivateKey] instance from the given DER-encoded byte array.
         *
         * The provided byte array must contain a valid private key in binary DER
         * format. No PEM parsing or validation is performed by this method; the
         * bytes are used as-is.
         *
         * @param byteArray The DER-encoded private key bytes.
         * @return A parsed [PrivateKey] instance.
         */
        fun fromByteArray(byteArray: ByteArray): PrivateKey {
            return PrivateKey(byteArray)
        }
    }
}
