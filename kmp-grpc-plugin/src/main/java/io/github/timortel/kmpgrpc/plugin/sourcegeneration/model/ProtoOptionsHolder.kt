package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Option
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

interface ProtoOptionsHolder : ProtoNode {

    val options: List<ProtoOption>
    val file: ProtoFile

    val parentOptionsHolder: ProtoOptionsHolder?

    val supportedOptions: List<Option<*>>

    override fun validate() {
        options.forEach { option ->
            val relatedOption = supportedOptions.firstOrNull { it.name == option.name }
            val isSupportedOnHolder = relatedOption != null
            val isIgnored = option.name in Options.ignoredOptions

            val languageConfiguration = relatedOption?.languageConfigurationMap?.get(file.languageVersion)

            val isSupportedOnLanguageVersion = when (languageConfiguration) {
                is Options.LangConfig.Available -> true
                is Options.LangConfig.Unavailable, null -> false
            }

            // An option can be supported, but still not valid, for example because it is only valid on certain field types
            val isValid = isSupportedOnHolder && isSupportedOnLanguageVersion && isSupportedOptionValid(option)

            if (!isIgnored && isValid) {
                when (languageConfiguration) {
                    is Options.LangConfig.Available -> {
                        val value = relatedOption.get(this)

                        if (languageConfiguration.isLocked && value != languageConfiguration.defaultValue) {
                            Warnings.unsupportedOptionValueUsed.withMessage(
                                "${option.name} at ${
                                    option.ctx.toFilePositionString(file.path)
                                } has fixed value of ${languageConfiguration.defaultValue}. Set value will be ignored."
                            )
                        }
                    }
                    is Options.LangConfig.Unavailable, null -> {}
                }
            }

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
