package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.ProtoReservation
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoMessage(
    override val name: String,
    val messages: List<ProtoMessage>,
    val enums: List<ProtoEnum>,
    val fields: List<ProtoMessageField>,
    val oneOfs: List<ProtoOneOf>,
    val mapFields: List<ProtoMapField>,
    val reservation: ProtoReservation,
    val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, DeclarationResolver {

    override lateinit var parent: ProtoDeclParent

    override val isNested: Boolean
        get() = when (parent) {
            is ProtoDeclParent.Message -> true
            is ProtoDeclParent.File -> super.isNested
        }

    override val file: ProtoFile
        get() {
            return when (val p = parent) {
                is ProtoDeclParent.File -> p.file
                is ProtoDeclParent.Message -> p.message.file
            }
        }

    override val candidates: List<DeclarationResolver.Candidate> =
        messages.map { DeclarationResolver.Candidate.Message(it) } + enums.map { DeclarationResolver.Candidate.Enum(it) }

    /**
     * True if the message has no fields
     */
    val isEmpty: Boolean =
        fields.isEmpty() && mapFields.isEmpty() && oneOfs.isEmpty()

    init {
        val parent = ProtoDeclParent.Message(this)

        messages.forEach { it.parent = parent }
        enums.forEach { it.parent = parent }

        oneOfs.forEach { it.message = this }
        fields.forEach { it.parent = this }
        mapFields.forEach { it.message = this }
    }

    override fun resolveDeclarationInParent(type: ProtoType.DefType): ProtoDeclaration? {
        return when (val p = parent) {
            is ProtoDeclParent.File -> p.file.resolveDeclaration(type)
            is ProtoDeclParent.Message -> p.message.resolveDeclaration(type)
        }
    }
}
