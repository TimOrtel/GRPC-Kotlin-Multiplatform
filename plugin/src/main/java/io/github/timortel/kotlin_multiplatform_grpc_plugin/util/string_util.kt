package io.github.timortel.kotlin_multiplatform_grpc_plugin.util

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.camelToSnakeUpperCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.uppercase()
}