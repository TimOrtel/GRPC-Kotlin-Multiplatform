package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import com.squareup.kotlinpoet.CodeBlock
import java.util.*

fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }
}

fun String.decapitalize(): String {
    return replaceFirstChar {
        if (it.isUpperCase()) it.lowercase(
            Locale.getDefault()
        ) else it.toString()
    }
}

fun <T> List<T>.joinToCodeBlock(separator: String, append: CodeBlock.Builder.(T) -> Unit): CodeBlock {
    return CodeBlock.builder().apply {
        forEachIndexed { index, value ->
            if (index != 0) add(separator)
            append(this@apply, value)
        }
    }.build()
}

fun String.snakeCaseToCamelCase(): String {
    return split('_').joinToString(separator = "") { it.capitalize() }
}
