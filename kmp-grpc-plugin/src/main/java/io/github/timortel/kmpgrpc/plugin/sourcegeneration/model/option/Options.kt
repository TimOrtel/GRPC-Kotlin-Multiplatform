package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoDefaultSymbolVisibility
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldPresence
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoRepeatedFieldEncoding

object Options {

    object Basic {
        val javaMultipleFiles = SimpleProtoOption(
            name = "java_multiple_files",
            parse = String::toBooleanStrictOrNull,
            targets = listOf(OptionTargetMatcher.FILE),
            proto3Config = LangConfig.Available(defaultValue = false),
            edition2023Config = LangConfig.Available(defaultValue = false),
            edition2024Config = LangConfig.Available(defaultValue = false)
        )

        val javaPackage = SimpleProtoOption(
            name = "java_package",
            parse = { it },
            targets = listOf(OptionTargetMatcher.FILE),
            proto3Config = LangConfig.Available(defaultValue = null),
            editionConfig = LangConfig.Available(defaultValue = null)
        )

        val javaOuterClassName = SimpleProtoOption(
            name = "java_outer_classname",
            parse = { it },
            targets = listOf(OptionTargetMatcher.FILE),
            proto3Config = LangConfig.Available(defaultValue = null),
            editionConfig = LangConfig.Available(defaultValue = null)
        )

        val allowAlias = SimpleProtoOption(
            name = "allow_alias",
            parse = String::toBooleanStrictOrNull,
            targets = listOf(OptionTargetMatcher.ENUM(restrictToTopLevel = false)),
            proto3Config = LangConfig.Available(defaultValue = false),
            editionConfig = LangConfig.Available(defaultValue = false)
        )

        val deprecated = SimpleProtoOption(
            name = "deprecated",
            parse = String::toBooleanStrictOrNull,
            targets = listOf(OptionTargetMatcher.FIELD(), OptionTargetMatcher.ENUM_ENTRY),
            proto3Config = LangConfig.Available(defaultValue = false),
            editionConfig = LangConfig.Available(defaultValue = false),
            failOnInvalidTargetUsage = false
        )

        val packed = SimpleProtoOption(
            name = "packed",
            parse = String::toBooleanStrictOrNull,
            targets = listOf(OptionTargetMatcher.FIELD(restriction = OptionTargetMatcher.FIELD.Restriction.OnlyOnRepeated(forcePackable = true))),
            proto3Config = LangConfig.Available(defaultValue = true),
            editionConfig = LangConfig.Unavailable()
        )
    }

    object Feature {
        val fieldPresence = FeatureProtoOption(
            name = "field_presence",
            parse = { value -> ProtoFieldPresence.entries.firstOrNull { it.name == value } },
            edition2023Config = LangConfig.Available(defaultValue = ProtoFieldPresence.EXPLICIT),
            edition2024Config = LangConfig.Available(defaultValue = ProtoFieldPresence.EXPLICIT),
            targets = listOf(OptionTargetMatcher.FILE, OptionTargetMatcher.FIELD())
        )

        val repeatedFieldEncoding = FeatureProtoOption(
            name = "repeated_field_encoding",
            parse = { value -> ProtoRepeatedFieldEncoding.entries.firstOrNull { it.name == value } },
            edition2023Config = LangConfig.Available(defaultValue = ProtoRepeatedFieldEncoding.PACKED),
            edition2024Config = LangConfig.Available(defaultValue = ProtoRepeatedFieldEncoding.PACKED),
            targets = listOf(OptionTargetMatcher.FILE, OptionTargetMatcher.FIELD(OptionTargetMatcher.FIELD.Restriction.OnlyOnRepeated(forcePackable = true)))
        )

        val defaultSymbolVisibility = FeatureProtoOption(
            name = "default_symbol_visibility",
            parse = { value -> ProtoDefaultSymbolVisibility.entries.firstOrNull { it.name == value } },
            languageConfigurationMap = mapOf(
                ProtoLanguageVersion.PROTO3 to LangConfig.Available(
                    defaultValue = ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    isLocked = true
                ),
                ProtoLanguageVersion.EDITION2023 to LangConfig.Available(
                    defaultValue = ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    isLocked = true
                ),
                ProtoLanguageVersion.EDITION2024 to LangConfig.Available(defaultValue = ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL)
            ),
            targets = listOf(OptionTargetMatcher.FILE)
        )

        val nestInFileClass = FeatureProtoOption(
            name = "(pb.java).nest_in_file_class",
            parse = { value -> ProtoNestInFileClass.entries.firstOrNull { it.name == value } },
            targets = listOf(
                OptionTargetMatcher.MESSAGE(restrictToTopLevel = true),
                OptionTargetMatcher.ENUM(restrictToTopLevel = true),
                OptionTargetMatcher.SERVICE(
                    restrictToTopLevel = true
                )
            ),
            edition2023Config = LangConfig.Unavailable(),
            edition2024Config = LangConfig.Available(defaultValue = ProtoNestInFileClass.NO)
        )

        val enumType = FeatureProtoOption(
            name = "enum_type",
            parse = { value -> ProtoEnumType.entries.firstOrNull { it.name == value } },
            targets = listOf(OptionTargetMatcher.FILE, OptionTargetMatcher.ENUM()),
            edition2023Config = LangConfig.Available(defaultValue = ProtoEnumType.OPEN),
            edition2024Config = LangConfig.Available(defaultValue = ProtoEnumType.OPEN),
        )
    }

    val options = listOf(
        Basic.javaMultipleFiles,
        Basic.javaPackage,
        Basic.javaOuterClassName,
        Basic.allowAlias,
        Basic.deprecated,
        Basic.packed,
        Feature.fieldPresence,
        Feature.repeatedFieldEncoding,
        Feature.defaultSymbolVisibility,
        Feature.nestInFileClass,
        Feature.enumType
    )

    /**
     * Options that do not apply to Java/Kotlin
     */
    val ignoredOptions = listOf(
        "go_package",
        "objc_class_prefix",
        "csharp_namespace",
        "cc_enable_arenas"
    )


    sealed interface LangConfig<T> {
        class Unavailable<T> : LangConfig<T>

        /**
         * @param isLocked if the value can only have the [defaultValue]. If a different value is provided, a warning is printed.
         */
        data class Available<T>(val defaultValue: T, val isLocked: Boolean = false) : LangConfig<T>
    }
}
