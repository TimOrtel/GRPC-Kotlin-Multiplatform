package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException

object Options {

    val javaMultipleFiles = Option(
        name = "java_multiple_files",
        parse = String::toBooleanStrictOrNull,
        default = false
    )

    val javaPackage = Option(
        name = "java_package",
        parse = { it },
        default = null
    )

    val javaOuterClassName = Option(
        name = "java_outer_classname",
        parse = { it },
        default = null
    )

    val allowAlias = Option(
        name = "allow_alias",
        parse = String::toBooleanStrictOrNull,
        default = false
    )

    val deprecated = Option(
        name = "deprecated",
        parse = String::toBooleanStrictOrNull,
        default = false
    )

    val packed = Option(
        name = "packed",
        parse = String::toBooleanStrictOrNull,
        default = true
    )

    /**
     * Options that do not apply to Java/Kotlin
     */
    val ignoredOptions = listOf(
        "go_package",
        "objc_class_prefix",
        "csharp_namespace",
        "cc_enable_arenas"
    )

    class Option<T>(val name: String, val parse: (String) -> T?, val default: T) {
        fun get(optionsHolder: ProtoOptionsHolder): T {
            val match = optionsHolder.options.firstOrNull { it.name == name } ?: return default
            return parse(match.value) ?: throw CompilationException.OptionFailedParse(
                "Could not parse option value.",
                optionsHolder.file,
                match.ctx
            )
        }
    }
}
