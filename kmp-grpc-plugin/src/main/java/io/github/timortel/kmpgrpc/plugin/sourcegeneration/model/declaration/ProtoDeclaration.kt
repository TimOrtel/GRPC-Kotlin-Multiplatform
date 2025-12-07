package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoDeclParent
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options

/**
 * Base interface of both messages and enums
 */
sealed interface ProtoDeclaration : ProtoBaseDeclaration {

    /**
     * The parent node of this declaration.
     */
    val parent: ProtoDeclParent

    /**
     * The symbol visibility set for this declaration. Null if not supported (< edition 2024) or not set.
     */
    val symbolVisibility: ProtoSymbolVisibility?

    val isExported: Boolean
        get() = when (symbolVisibility) {
            ProtoSymbolVisibility.EXPORT -> true
            ProtoSymbolVisibility.LOCAL -> false
            null -> when (Options.Feature.defaultSymbolVisibility.get(this)) {
                ProtoDefaultSymbolVisibility.EXPORT_ALL -> true
                ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL -> isProtoTopLevel
                ProtoDefaultSymbolVisibility.LOCAL_ALL -> false
                ProtoDefaultSymbolVisibility.STRICT -> false
            }
        }

    /**
     * The type of this declaration as it will be generated
     */
    override val className: ClassName
        get() {
            return when (val p = parent) {
                is ProtoDeclParent.Message -> p.message.className.nestedClass(name)
                is ProtoDeclParent.File -> super.className
            }
        }

    override val isNested: Boolean
        get() = when (parent) {
            is ProtoDeclParent.Message -> true
            is ProtoDeclParent.File -> super.isNested
        }

    val isProtoTopLevel: Boolean
        get() = when (parent) {
            is ProtoDeclParent.File -> true
            is ProtoDeclParent.Message -> false
        }

    override fun validate() {
        super.validate()

        when (Options.Feature.defaultSymbolVisibility.get(this)) {
            ProtoDefaultSymbolVisibility.STRICT -> {
                if (symbolVisibility == ProtoSymbolVisibility.EXPORT && !isProtoTopLevel) {
                    throw CompilationException.StrictExportViolation(
                        message = "$name is nested and cannot be exported with STRICT default_symbol_visbility.",
                        file = file,
                        ctx = ctx
                    )
                }
            }

            else -> {}
        }

        if (symbolVisibility != null && file.languageVersion != ProtoLanguageVersion.EDITION2024) {
            throw CompilationException.UnsupportedLanguageFeatureUsed(
                message = "Symbol visibility ${symbolVisibility?.name} is not supported on ${file.languageVersion.name}",
                file = file,
                ctx = ctx
            )
        }
    }
}
