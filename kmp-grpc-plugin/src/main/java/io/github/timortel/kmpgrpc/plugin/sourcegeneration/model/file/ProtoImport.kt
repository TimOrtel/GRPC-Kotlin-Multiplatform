package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoImport(val identifier: String, val type: Type, val ctx: ParserRuleContext) : ProtoNode {

    lateinit var file: ProtoFile

    // Remove " from identifier
    val path: String = identifier.substring(1, identifier.length - 1)

    override fun validate() {
        when (type) {
            Type.PUBLIC -> {
                file.project.logger.warn(Warnings.unsupportedPublicImportUsed.withMessage(ctx, file, "Currently not supported"))
            }
            Type.OPTION -> {
                when (file.languageVersion) {
                    ProtoLanguageVersion.PROTO3, ProtoLanguageVersion.EDITION2023 -> throw CompilationException.UnsupportedLanguageFeatureUsed(
                        message = "Option imports are not available in language version ${file.languageVersion}",
                        file = file,
                        ctx = ctx
                    )
                    ProtoLanguageVersion.EDITION2024 -> {
                        file.project.logger.warn(Warnings.unsupportedOptionImportUsed.withMessage(ctx, file, "Currently not supported"))
                    }
                }
            }
            Type.DEFAULT -> {}
        }
    }

    enum class Type {
        DEFAULT,
        OPTION,
        PUBLIC
    }
}
