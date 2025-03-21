package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import java.util.*

fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }
}