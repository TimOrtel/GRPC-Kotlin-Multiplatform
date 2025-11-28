package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options.LangConfig

/**
 * Proto option that recursively checks for the option to be set and returns the first value found.
 * If no value is found, the default is returned.
 */
class FeatureProtoOption<T>(
    name: String,
    parse: (String) -> T?,
    languageConfigurationMap: Map<ProtoLanguageVersion, LangConfig<T>>
) : Option<T>(name, parse, languageConfigurationMap) {

    constructor(
        name: String,
        parse: (String) -> T?,
        proto3Config: LangConfig<T>,
        edition2023Config: LangConfig<T>
    ) : this(
        name = name,
        parse = parse,
        languageConfigurationMap = mapOf(
            ProtoLanguageVersion.PROTO3 to proto3Config,
            ProtoLanguageVersion.EDITION2023 to edition2023Config
        )
    )

    override fun get(optionsHolder: ProtoOptionsHolder): T {
        val match = optionsHolder.options.firstOrNull { it.name == name }
        val parent = optionsHolder.parentOptionsHolder

        return when {
            match != null -> attemptParse(match.value, optionsHolder.file, match.ctx)
            parent != null -> get(parent)
            // match is null and parent is null -> we did not find the option anywhere
            else -> getDefaultValue(optionsHolder.file.languageVersion)
        }
    }
}
