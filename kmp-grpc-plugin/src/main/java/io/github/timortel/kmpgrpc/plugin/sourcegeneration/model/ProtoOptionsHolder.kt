package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.MatchResult
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

interface ProtoOptionsHolder : ProtoNode {

    val options: List<ProtoOption>
    val file: ProtoFile

    val parentOptionsHolder: ProtoOptionsHolder?

    val optionTarget: OptionTarget

    override fun validate() {
        options.forEach { option ->
            val isIgnored = option.name in Options.ignoredOptions
            val relatedOption = Options.options.firstOrNull { it.name == option.name }

            if (relatedOption == null) {
                if (!isIgnored) {
                    file.project.logger.warn(
                        Warnings.unsupportedOptionUsed.withMessage(
                            "${option.name} at ${
                                option.ctx.toFilePositionString(file.path)
                            }"
                        )
                    )
                }
            } else {
                val throwInvalidOptionTargetException = { message: String ->
                    throw CompilationException.OptionInvalidTarget(
                        message = message,
                        file = file,
                        ctx = option.ctx
                    )
                }

                when (val languageConfiguration = relatedOption.languageConfigurationMap[file.languageVersion]) {
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
                    is Options.LangConfig.Unavailable, null -> {
                        throw CompilationException.OptionUsedWithInvalidLanguageVersion(
                            message = "${option.name} is not supported on language version ${file.languageVersion}",
                            file = file,
                            ctx = option.ctx
                        )
                    }
                }

                val targetMatcher = relatedOption.targetMatchers.firstOrNull { it.target == optionTarget::class }
                if (targetMatcher == null) throwInvalidOptionTargetException("${option.name} is not supported on $optionTarget")

                when (val matchResult = targetMatcher.matches(optionTarget)) {
                    MatchResult.Success -> {}
                    is MatchResult.Failure -> {
                        throwInvalidOptionTargetException(matchResult.reason)
                    }
                }
            }
        }

        options
            .groupBy { it.name }
            .filter { it.value.size > 1 }
            .forEach { (name, options) ->
                val relatedOption = Options.options.firstOrNull { it.name == name }
                if (relatedOption != null) {
                    val message = buildString {
                        append("Found clashing proto options for $name:\n")
                        options.joinToString(separator = "\n") {
                            "-> $name at ${it.ctx.toFilePositionString(file.path)}"
                        }
                    }

                    throw CompilationException.DuplicateDeclaration(message, file)
                }
            }
    }
}
