package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.toFilePositionString

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
