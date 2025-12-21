package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.BaseDeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoDeclParent
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.enumeration.ProtoEnumField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.ProtoEnumType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnum(
    override val name: String,
    val fields: List<ProtoEnumField>,
    override val options: List<ProtoOption>,
    override val reservation: ProtoReservation,
    override val symbolVisibility: ProtoSymbolVisibility?,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, BaseDeclarationResolver, ProtoFieldHolder {

    companion object {
        const val UNRECOGNIZED_FIELD_NAME = "UNRECOGNIZED"
    }

    override lateinit var parent: ProtoDeclParent

    override val file: ProtoFile
        get() = when (val p = parent) {
            is ProtoDeclParent.File -> p.file
            is ProtoDeclParent.Message -> p.message.file
        }

    val defaultField: ProtoEnumField
        get() =
            fields
                .firstOrNull { it.number == 0 }
                ?: throw CompilationException.EnumIllegalFirstField(
                    "Enumeration does not have field with value 0.",
                    file,
                    ctx
                )

    override val heldFields: List<ProtoField> =
        fields

    override val optionTarget: OptionTarget get() = OptionTarget.ENUM(isProtoTopLevel)

    override val parentOptionsHolder: ProtoOptionsHolder
        get() = when (val p = parent) {
            is ProtoDeclParent.File -> p.file
            is ProtoDeclParent.Message -> p.message
        }

    init {
        fields.forEach { it.enum = this }
    }

    // Implements: https://protobuf.dev/programming-guides/enum/#spec
    /**
     * @param userLanguage the language of whoever wants to use the enum.
     */
    fun isOpen(userLanguage: ProtoLanguageVersion): Boolean {
        val getFeatureIsOpen = {
            when (Options.Feature.enumType.get(this)) {
                ProtoEnumType.OPEN -> true
                ProtoEnumType.CLOSED -> false
            }
        }

        return when (userLanguage) {
            ProtoLanguageVersion.PROTO3 -> when (file.languageVersion) {
                ProtoLanguageVersion.PROTO3 -> true
                ProtoLanguageVersion.EDITION2023, ProtoLanguageVersion.EDITION2024 -> getFeatureIsOpen()
            }
            ProtoLanguageVersion.EDITION2023, ProtoLanguageVersion.EDITION2024 -> when (file.languageVersion) {
                ProtoLanguageVersion.PROTO3 -> true
                ProtoLanguageVersion.EDITION2023, ProtoLanguageVersion.EDITION2024 -> getFeatureIsOpen()
            }
        }
    }

    override fun validate() {
        super<ProtoDeclaration>.validate()
        super<ProtoFieldHolder>.validate()

        if (fields.isEmpty()) throw CompilationException.EnumNoFields(
            message = "Proto enumeration does not have any values.",
            file = file,
            ctx = ctx
        )

        if (fields.first().number != 0) throw CompilationException.EnumIllegalFirstField(
            message = "The first value defined in an enumeration must have value 0",
            file = file,
            ctx = ctx
        )

        val allowAlias = Options.Basic.allowAlias.get(this)
        fields
            .groupBy { it.number }
            .filter { it.value.size > 1 }
            .forEach { (_, fields) ->
                if (!allowAlias) {
                    val warning = Warnings.enumAliasWithoutOption.withMessage(
                        message = fields.joinToString(separator = "\n") {
                            "-> ${it.name} = ${it.number} at ${
                                it.ctx.toFilePositionString(
                                    file.path
                                )
                            }"
                        }
                    )

                    file.project.logger.warn(warning)
                }
            }

        fields.forEach { it.validate() }
    }

    // An enum does not have children, so it cannot resolve anything
    override fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration? {
        return null
    }
}
