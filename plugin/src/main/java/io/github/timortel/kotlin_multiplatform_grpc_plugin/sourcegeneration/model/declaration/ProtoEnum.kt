package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.Warnings
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.enumeration.ProtoEnumField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.toFilePositionString
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnum(
    override val name: String,
    val fields: List<ProtoEnumField>,
    val options: List<ProtoOption>,
    val reservation: ProtoReservation,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, BaseDeclarationResolver, ProtoNode {

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
                ?: throw CompilationException.EnumIllegalFirstField("Enumeration does not have field with value 0.", file, ctx)

    init {
        fields.forEach { it.enum = this }
    }

    override fun validate() {
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

        val allowAlias = Options.allowAlias.get(file, options)
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

        fields.filter { it.number in reservation }.forEach { field ->
            throw CompilationException.ReservedFieldNumberUsed(
                message = "Usage of reserved field number not permitted. ${field.name} uses reserved number ${field.number}",
                file = file,
                ctx = field.ctx
            )
        }

        fields.filter { it.name in reservation }.forEach { field ->
            throw CompilationException.ReservedFieldNameUsed(
                message = "Usage of reserved field name not permitted. ${field.name} uses reserved name ${field.name}",
                file = file,
                ctx = field.ctx
            )
        }
    }

    // An enum does not have children, so it cannot resolve anything
    override fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration? {
        return null
    }
}
