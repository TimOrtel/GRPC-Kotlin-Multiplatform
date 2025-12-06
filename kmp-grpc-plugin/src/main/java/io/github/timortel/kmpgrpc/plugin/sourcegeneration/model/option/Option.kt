package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options.LangConfig
import org.antlr.v4.runtime.ParserRuleContext

abstract class Option<T>(
    val name: String,
    val parse: (String) -> T?,
    val languageConfigurationMap: Map<ProtoLanguageVersion, LangConfig<T>>,
    val targetMatchers: List<OptionTargetMatcher>
) {

    init {
        require(languageConfigurationMap.keys == ProtoLanguageVersion.entries.toSet())
    }

    abstract fun get(optionsHolder: ProtoOptionsHolder): T

    protected fun attemptParse(value: String, file: ProtoFile, ctx: ParserRuleContext): T {
        return parse(value) ?: throw CompilationException.OptionFailedParse(
            "Could not parse option value.",
            file,
            ctx
        )
    }

    protected fun getDefaultValue(languageVersion: ProtoLanguageVersion): T {
        return when (val config = languageConfigurationMap[languageVersion]) {
            is LangConfig.Available -> config.defaultValue
            is LangConfig.Unavailable -> throw RuntimeException("The proto enum option $name is not available on language version $languageVersion")
            null -> throw RuntimeException("No default value provided for $languageVersion.")
        }
    }
}
