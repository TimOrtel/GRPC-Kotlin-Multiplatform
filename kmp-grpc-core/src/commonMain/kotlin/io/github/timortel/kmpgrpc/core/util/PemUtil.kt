package io.github.timortel.kmpgrpc.core.util

import kotlin.io.encoding.Base64

private val pemRegex =
    "-----BEGIN ([A-Z ]+)-----(\\s*([A-Za-z0-9+/=\\r\\n]+?)\\s*)-----END \\1-----(\\r?\\n)?".toRegex()

internal fun parseBytesFromPemContent(pemContent: String, expectedContainer: String): ByteArray {
    val matchResult = pemRegex.matchEntire(pemContent)
        ?: throw IllegalArgumentException("Input does not contain a valid PEM-encoded certificate.")
    val container = matchResult.groups[1]!!.value
    if (container != expectedContainer) throw IllegalArgumentException("Received PEM file with header $container but expected $expectedContainer.")

    val pemBody = (matchResult.groups[2]?.value!!).replace("\n", "")
    return Base64.decode(pemBody)
}

internal fun bytesAsPemContent(bytes: ByteArray, container: String): String {
    return buildString {
        append("-----BEGIN ")
        append(container)
        append("-----\n")

        Base64.encode(bytes).chunked(64).forEach { chunk ->
            append(chunk)
            append("\n")
        }

        append("-----END ")
        append(container)
        append("-----\n")
    }
}
