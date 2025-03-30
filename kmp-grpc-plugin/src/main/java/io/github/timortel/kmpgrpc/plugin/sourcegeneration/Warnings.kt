package io.github.timortel.kmpgrpc.plugin.sourcegeneration

object Warnings {

    val enumAliasWithoutOption = Warning("Enum alias detected but option allow_alias not set")

    val unsupportedOptionUsed = Warning("Unsupported option set")

    class Warning(prefix: String) {
        private val prefix: String = "Warning: $prefix"

        fun withMessage(message: String): String = "$prefix - $message"

        fun isWarning(warning: String): Boolean = warning.startsWith(prefix)
    }
}