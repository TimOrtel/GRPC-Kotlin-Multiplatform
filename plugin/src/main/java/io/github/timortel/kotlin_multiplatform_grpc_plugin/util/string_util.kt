package io.github.timortel.kotlin_multiplatform_grpc_plugin.util

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.capitalize

fun String.snakeCaseToCamelCase(): String {
    return split('_').joinToString(separator = "") { it.capitalize() }
}
