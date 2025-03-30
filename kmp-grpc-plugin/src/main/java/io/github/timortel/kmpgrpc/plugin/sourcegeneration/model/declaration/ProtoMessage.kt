package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoMessage(
    override val name: String,
    val messages: List<ProtoMessage>,
    val enums: List<ProtoEnum>,
    val fields: List<ProtoMessageField>,
    val oneOfs: List<ProtoOneOf>,
    val mapFields: List<ProtoMapField>,
    override val reservation: ProtoReservation,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, FileBasedDeclarationResolver, ProtoFieldHolder {

    override lateinit var parent: ProtoDeclParent

    override val file: ProtoFile
        get() {
            return when (val p = parent) {
                is ProtoDeclParent.File -> p.file
                is ProtoDeclParent.Message -> p.message.file
            }
        }

    override val candidates: List<DeclarationResolver.Candidate> =
        messages.map { DeclarationResolver.Candidate.Message(it) } + enums.map { DeclarationResolver.Candidate.Enum(it) }

    override val heldFields: List<ProtoField> =
        fields + mapFields + oneOfs.flatMap { it.fields }

    /**
     * True if the message has no fields
     */
    val isEmpty: Boolean =
        fields.isEmpty() && mapFields.isEmpty() && oneOfs.isEmpty()

    override val supportedOptions: List<Options.Option<*>> = emptyList()

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

    override fun validate() {
        super<FileBasedDeclarationResolver>.validate()
        super<ProtoFieldHolder>.validate()
        super<ProtoDeclaration>.validate()

        messages.forEach { it.validate() }
        enums.forEach { it.validate() }

        fields.forEach { it.validate() }
        mapFields.forEach { it.validate() }
        oneOfs.forEach { it.validate() }
    }
}
