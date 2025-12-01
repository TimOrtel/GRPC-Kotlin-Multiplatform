package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options.LangConfig

/**
 * Proto option implementation that only looks up the value on the given [ProtoOptionsHolder].
 */
class SimpleProtoOption<T>(
    name: String,
    parse: (String) -> T?,
    languageConfigurationMap: Map<ProtoLanguageVersion, LangConfig<T>>,
    targets: List<OptionTarget>
) : Option<T>(name, parse, languageConfigurationMap, targets) {

    constructor(
        name: String,
        parse: (String) -> T?,
        targets: List<OptionTarget>,
        proto3Config: LangConfig<T>,
        edition2023Config: LangConfig<T>
    ) : this(
        name = name,
        parse = parse,
        languageConfigurationMap = mapOf(
            ProtoLanguageVersion.PROTO3 to proto3Config,
            ProtoLanguageVersion.EDITION2023 to edition2023Config
        ),
        targets = targets
    )

    override fun get(optionsHolder: ProtoOptionsHolder): T {
        val match = optionsHolder.options.firstOrNull { it.name == name }
            ?: return getDefaultValue(optionsHolder.file.languageVersion)
        return attemptParse(match.value, optionsHolder.file, match.ctx)
    }
}
