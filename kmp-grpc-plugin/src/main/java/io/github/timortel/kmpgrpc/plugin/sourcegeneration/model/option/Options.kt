package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

object Options {

    val javaMultipleFiles = SimpleProtoOption(
        name = "java_multiple_files",
        parse = String::toBooleanStrictOrNull,
        proto3Config = LangConfig.Available(defaultValue = false),
        edition2023Config = LangConfig.Available(defaultValue = false)
    )

    val javaPackage = SimpleProtoOption(
        name = "java_package",
        parse = { it },
        proto3Config = LangConfig.Available(defaultValue = null),
        edition2023Config = LangConfig.Available(defaultValue = null)
    )

    val javaOuterClassName = SimpleProtoOption(
        name = "java_outer_classname",
        parse = { it },
        proto3Config = LangConfig.Available(defaultValue = null),
        edition2023Config = LangConfig.Available(defaultValue = null)
    )

    val allowAlias = SimpleProtoOption(
        name = "allow_alias",
        parse = String::toBooleanStrictOrNull,
        proto3Config = LangConfig.Available(defaultValue = false),
        edition2023Config = LangConfig.Available(defaultValue = false)
    )

    val deprecated = SimpleProtoOption(
        name = "deprecated",
        parse = String::toBooleanStrictOrNull,
        proto3Config = LangConfig.Available(defaultValue = false),
        edition2023Config = LangConfig.Available(defaultValue = false)
    )

    val packed = SimpleProtoOption(
        name = "packed",
        parse = String::toBooleanStrictOrNull,
        proto3Config = LangConfig.Available(defaultValue = true),
        edition2023Config = LangConfig.Available(defaultValue = true, isLocked = true)
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