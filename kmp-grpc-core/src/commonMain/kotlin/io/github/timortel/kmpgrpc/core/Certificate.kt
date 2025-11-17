package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.util.bytesAsPemContent
import io.github.timortel.kmpgrpc.core.util.parseBytesFromPemContent

/**
 * Represents an X.509 certificate.
 */
class Certificate internal constructor(internal val data: ByteArray)  {

    internal val asPem: String get() = bytesAsPemContent(data, PEM_CONTAINER)

    companion object {
        private const val PEM_CONTAINER = "CERTIFICATE"

        /**
         * Creates a [Certificate] instance from the given PEM-formatted text.
         *
         * This method validates that the input string contains a single
         * certificate enclosed in the standard PEM markers. If validation
         * succeeds, the certificate is parsed and returned.
         *
         * @param content The PEM-encoded certificate string.
         * @return A parsed [Certificate] instance.
         *
         * @throws IllegalArgumentException If the input does not contain a valid
         * PEM certificate.
         */
        fun fromPem(content: String): Certificate {
            return Certificate(parseBytesFromPemContent(content, PEM_CONTAINER))
        }

        /**
         * Creates a [Certificate] instance from the given DER-encoded byte array.
         *
         * The provided data must contain a valid X.509 certificate in binary
         * DER format. No PEM parsing is performed by this method; the bytes are
         * used as-is.
         *
         * @param byteArray The DER-encoded X.509 certificate bytes.
         * @return A parsed [Certificate] instance.
         */
        fun fromByteArray(byteArray: ByteArray): Certificate {
            return Certificate(byteArray)
        }
    }
}
