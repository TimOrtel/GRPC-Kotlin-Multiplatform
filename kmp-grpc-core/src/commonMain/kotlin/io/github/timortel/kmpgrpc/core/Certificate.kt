package io.github.timortel.kmpgrpc.core

import kotlin.io.encoding.Base64

/**
 * Represents an X.509 certificate provided in PEM format.
 *
 * This class parses and validates the PEM content upon construction. The
 * certificate must contain a properly formatted `-----BEGIN CERTIFICATE-----`
 * and `-----END CERTIFICATE-----` block with valid Base64-encoded DER data
 * inside.
 */
class Certificate internal constructor(internal val pemContent: String) {

    private val pemBody: String
    internal val bytes: ByteArray get() = Base64.decode(pemBody)

    init {
        val matchResult = pemRegex.matchEntire(pemContent) ?: throw IllegalArgumentException("Input does not contain a valid PEM-encoded certificate.")
        pemBody = (matchResult.groups[1]?.value!!).replace("\n", "")
    }

    companion object {
        private val pemRegex =
            "-----BEGIN CERTIFICATE-----(\\s*([A-Za-z0-9+/=\\r\\n]+?)\\s*)-----END CERTIFICATE-----(\\r?\\n)?".toRegex()

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
            return Certificate(content)
        }
    }
}
