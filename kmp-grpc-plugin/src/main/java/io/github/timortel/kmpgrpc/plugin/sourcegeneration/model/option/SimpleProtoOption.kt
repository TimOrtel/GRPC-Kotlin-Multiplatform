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
    targets: List<OptionTargetMatcher>,
    failOnInvalidTargetUsage: Boolean
) : Option<T>(name, parse, languageConfigurationMap, targets, failOnInvalidTargetUsage) {

    constructor(
        name: String,
        parse: (String) -> T?,
        targets: List<OptionTargetMatcher>,
        proto3Config: LangConfig<T>,
        editionConfig: LangConfig<T>,
        failOnInvalidTargetUsage: Boolean = true
    ) : this(
        name = name,
        parse = parse,
        proto3Config = proto3Config,
        edition2023Config = editionConfig,
        edition2024Config = editionConfig,
        targets = targets,
        failOnInvalidTargetUsage = failOnInvalidTargetUsage
    )

    constructor(
        name: String,
        parse: (String) -> T?,
        targets: List<OptionTargetMatcher>,
        proto3Config: LangConfig<T>,
        edition2023Config: LangConfig<T>,
        edition2024Config: LangConfig<T>,
        failOnInvalidTargetUsage: Boolean = true
    ) : this(
        name = name,
        parse = parse,
        languageConfigurationMap = mapOf(
            ProtoLanguageVersion.PROTO3 to proto3Config,
            ProtoLanguageVersion.EDITION2023 to edition2023Config,
            ProtoLanguageVersion.EDITION2024 to edition2024Config
        ),
        targets = targets,
        failOnInvalidTargetUsage = failOnInvalidTargetUsage
    )

    override fun get(optionsHolder: ProtoOptionsHolder): T {
        val match = optionsHolder.options.firstOrNull { it.name == name }
            ?: return getDefaultValue(optionsHolder.file.languageVersion)
        return attemptParse(match.value, optionsHolder.file, match.ctx)
    }
}
