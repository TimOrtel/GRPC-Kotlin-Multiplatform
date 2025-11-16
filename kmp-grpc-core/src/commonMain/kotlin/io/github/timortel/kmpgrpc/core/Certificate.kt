package io.github.timortel.kmpgrpc.core

import kotlin.io.encoding.Base64

class Certificate internal constructor(val pemContent: String) {

    private val pemBody: String
    internal val bytes: ByteArray get() = Base64.decode(pemBody)

    init {
        val matchResult = pemRegex.matchEntire(pemContent) ?: throw IllegalArgumentException("Input does not contain a valid PEM-encoded certificate.")
        pemBody = (matchResult.groups[1]?.value!!).replace("\n", "")
    }

    companion object {
        private val pemRegex =
            "-----BEGIN CERTIFICATE-----(\\s*([A-Za-z0-9+/=\\r\\n]+?)\\s*)-----END CERTIFICATE-----(\\r?\\n)?".toRegex()

        fun fromPem(content: String): Certificate {
            return Certificate(content)
        }
    }
}
