package io.github.timortel.kmpgrpc.plugin.sourcegeneration.util

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

fun List<CodeBlock>.joinCodeBlocks(separator: String): CodeBlock {
    return joinCodeBlocks(CodeBlock.of(separator))
}

fun List<CodeBlock>.joinCodeBlocks(separator: CodeBlock): CodeBlock {
    return filter { it.isNotEmpty() }.joinToCodeBlock(separator) { add(it) }
}

fun <T> List<T>.joinToCodeBlock(separator: String, append: CodeBlock.Builder.(T) -> Unit): CodeBlock {
    return joinToCodeBlock(CodeBlock.of(separator), append)
}

fun <T> List<T>.joinToCodeBlock(separator: CodeBlock, append: CodeBlock.Builder.(T) -> Unit): CodeBlock {
    return CodeBlock.builder().apply {
        forEachIndexed { index, value ->
            if (index != 0) add(separator)
            add(
                CodeBlock.builder()
                    .apply { append(value) }
                    .build()
            )
        }
    }.build()
}

fun String.snakeCaseToCamelCase(): String {
    return split('_').joinToString(separator = "") { it.capitalize() }
}
