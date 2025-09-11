package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

/**
 * Shared logic of checking for duplicated fields and that no reserved field numbers and names are used.
 */
interface ProtoFieldHolder : ProtoNode {

    val heldFields: List<ProtoField>

    val reservation: ProtoReservation

    val file: ProtoFile

    /**
     * Validate for duplicated fields and reserved field usages.
     */
    override fun validate() {
        heldFields.forEach { it.validate() }

        // Look for duplicated fields
        heldFields
            .groupBy { it.name }
            .filter { (_, values) -> values.size > 1 }
            .forEach { (name, values) ->
                val message = buildString {
                    append("Duplicated fields detected for $name:\n")
                    append(
                        values.joinToString("\n") { "-> ${it.ctx.text} at ${it.ctx.toFilePositionString(file.path)}" }
                    )
                }

                throw CompilationException.DuplicateDeclaration(message, file)
            }

        heldFields.filter { it.number in reservation }.forEach { field ->
            throw CompilationException.ReservedFieldNumberUsed(
                message = "Usage of reserved field number not permitted. ${field.name} uses reserved number ${field.number}",
                file = file,
                ctx = field.ctx
            )
        }

        heldFields.filter { it.name in reservation }.forEach { field ->
            throw CompilationException.ReservedFieldNameUsed(
                message = "Usage of reserved field name not permitted. ${field.name} uses reserved name ${field.name}",
                file = file,
                ctx = field.ctx
            )
        }
    }
}
