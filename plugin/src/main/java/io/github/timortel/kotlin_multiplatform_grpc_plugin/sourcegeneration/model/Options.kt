package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.Protobuf3CompilationException
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

    class Option<T>(val name: String, val parse: (String) -> T?, val default: T) {
        fun get(file: ProtoFile, options: List<ProtoOption> = file.options): T {
            val matches = options.filter { it.name == name }
            if (matches.size > 1) {
                val message = buildString {
                    append("Found clashing proto options for $name:\n")
                    matches.joinToString(separator = "\n") {
                        "-> $name at ${it.ctx.toFilePositionString(file.path)}"
                    }
                }

                throw Protobuf3CompilationException(message, file)
            }

            val match = matches.firstOrNull() ?: return default
            return parse(match.value) ?: throw Protobuf3CompilationException(
                "Could not parse option value.",
                file,
                match.ctx
            )
        }
    }
}
