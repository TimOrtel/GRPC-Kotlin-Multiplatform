package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

interface ProtoOptionsHolder : ProtoNode {

    val options: List<ProtoOption>
    val file: ProtoFile

    val supportedOptions: List<Options.Option<*>>

    override fun validate() {
        options.forEach { option ->
            val isSupported = option.name in supportedOptions.map { it.name }
            val isIgnored = option.name in Options.ignoredOptions

            // An option can be supported, but still not valid, for example because it is only valid on certain field types
            val isValid = isSupported && isSupportedOptionValid(option)

            if (!isIgnored && !isValid) {
                file.project.logger.warn(
                    Warnings.unsupportedOptionUsed.withMessage(
                        "${option.name} at ${
                            option.ctx.toFilePositionString(file.path)
                        }"
                    )
                )
            }
        }

        options
            .groupBy { it.name }
            .filter { it.value.size > 1 }
            .forEach { (name, options) ->
                val message = buildString {
                    append("Found clashing proto options for $name:\n")
                    options.joinToString(separator = "\n") {
                        "-> $name at ${it.ctx.toFilePositionString(file.path)}"
                    }
                }

                throw CompilationException.DuplicateDeclaration(message, file)
            }
    }

    fun isSupportedOptionValid(option: ProtoOption): Boolean = true
}
