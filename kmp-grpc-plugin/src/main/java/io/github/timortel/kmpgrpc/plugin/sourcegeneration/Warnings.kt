package io.github.timortel.kmpgrpc.plugin.sourcegeneration

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString
import org.antlr.v4.runtime.ParserRuleContext

object Warnings {

    val enumAliasWithoutOption = Warning("Enum alias detected but option allow_alias not set")

    val unsupportedOptionUsed = Warning("Unsupported option set")

    val unsupportedOptionValueUsed = Warning("Locked option value overwritten")

    val unsupportedPublicImportUsed = Warning("Public import usage")

    val unsupportedOptionImportUsed = Warning("Option import usage")

    class Warning(prefix: String) {
        private val prefix: String = "Warning: $prefix"

        fun withMessage(message: String): String = "$prefix - $message"

        fun withMessage(ctx: ParserRuleContext, file: ProtoFile, message: String): String = withMessage("${ctx.toFilePositionString(file.path)}: $message")

        fun isWarning(warning: String): Boolean = warning.startsWith(prefix)
    }
}
