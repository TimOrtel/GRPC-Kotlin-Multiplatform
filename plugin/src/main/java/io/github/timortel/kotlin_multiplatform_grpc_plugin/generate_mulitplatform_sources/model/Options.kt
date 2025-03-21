package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Protobuf3CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.toFilePositionString

object Options {
    val javaUseMultipleFiles = Option(
        name = "java_use_multiple_files",
        parse = String::toBooleanStrictOrNull,
        default = false
    )

    class Option<T>(val name: String, val parse: (String) -> T?, val default: T & Any) {
        fun get(file: ProtoFile, options: List<ProtoOption>): T {
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
            return parse(match.value) ?: throw Protobuf3CompilationException("Could not parse option value.", file, match.ctx)
        }
    }
}
