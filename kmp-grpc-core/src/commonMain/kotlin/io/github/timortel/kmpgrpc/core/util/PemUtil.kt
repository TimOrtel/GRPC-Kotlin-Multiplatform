package io.github.timortel.kmpgrpc.core.util

import kotlin.io.encoding.Base64

private val pemRegex =
    "-----BEGIN ([A-Z ]+)-----[\\s\\r\\n]*([A-Za-z0-9+/=\\r\\n]+?)[\\s\\r\\n]*-----END \\1-----".toRegex()

internal fun parseBytesFromPemContent(pemContent: String, expectedContainer: String): ByteArray {
    val matchResult = pemRegex.matchEntire(pemContent)
        ?: throw IllegalArgumentException("Input does not contain a valid PEM-encoded certificate.")
    val container = matchResult.groups[1]!!.value
    if (container != expectedContainer) throw IllegalArgumentException("Received PEM file with header $container but expected $expectedContainer.")

    val pemBody = (matchResult.groups[2]?.value!!).replace(Regex("[\\s\\r\\n]"), "")
    return Base64.decode(pemBody)
}

internal fun bytesAsPemContent(bytes: ByteArray, container: String): String {
    return buildString {
        append("-----BEGIN ")
        append(container)
        append("-----\n")

        val base64 = Base64.encode(bytes)
        var start = 0
        while (start < base64.length) {
            val end = minOf(start + 64, base64.length)
            appendRange(base64, start, end)
            append('\n')
            start = end
        }

        append("-----END ")
        append(container)
        append("-----\n")
    }
}
