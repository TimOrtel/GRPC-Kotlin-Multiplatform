package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.MemberName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoExtensionRanges
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.decapitalize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Representation of a proto message.
 */
data class ProtoMessage(
    override val name: String,
    override val messages: List<ProtoMessage>,
    val enums: List<ProtoEnum>,
    val fields: List<ProtoMessageField>,
    val oneOfs: List<ProtoOneOf>,
    val mapFields: List<ProtoMapField>,
    override val reservation: ProtoReservation,
    override val options: List<ProtoOption>,
    override val extensionDefinitions: List<ProtoExtensionDefinition>,
    val extensionRange: ProtoExtensionRanges,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, FileBasedDeclarationResolver, ProtoFieldHolder, ProtoChildPropertyNameResolver,
    ProtoExtensionDefinitionHolder, ProtoExtensionDefinitionFinder {

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

    override val optionTarget: OptionTarget = OptionTarget.MESSAGE

    override val parentOptionsHolder: ProtoOptionsHolder
        get() = when (val p = parent) {
            is ProtoDeclParent.File -> p.file
            is ProtoDeclParent.Message -> p.message
        }

    /**
     * The full proto name of this message
     */
    val fullName: String
        get() {
            val protoPackage = file.`package`

            return if (!protoPackage.isNullOrEmpty()) {
                "$protoPackage.$name"
            } else name
        }

    val dslBuildFunction: MemberName
        get() = MemberName(file.javaPackage, name.decapitalize())

    override val childProperties: List<ProtoChildProperty>
        get() = fields + mapFields + oneOfs + fields.flatMap { it.childProperties }

    override val reservedAttributeNames: Set<String>
        get() = Const.Message.reservedAttributeNames

    val extensionsInProject: List<ProtoExtensionDefinition>
        get() = project.findExtensionDefinitionsForMessage(this)

    val isExtendable: Boolean = extensionRange.ranges.isNotEmpty()

    init {
        val parent = ProtoDeclParent.Message(this)

        messages.forEach { it.parent = parent }
        enums.forEach { it.parent = parent }

        oneOfs.forEach { it.message = this }
        fields.forEach { it.parent = ProtoMessageField.Parent.Message(this) }
        mapFields.forEach { it.message = this }

        extensionDefinitions.forEach { it.parent = ProtoExtensionDefinition.Parent.Message(this) }

        extensionRange.message = this
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
        super<ProtoExtensionDefinitionHolder>.validate()

        messages.forEach { it.validate() }
        enums.forEach { it.validate() }

        fields.forEach { it.validate() }
        mapFields.forEach { it.validate() }
        oneOfs.forEach { it.validate() }

        val extensionsInProject = extensionsInProject
        if (!isExtendable && extensionsInProject.isNotEmpty()) {
            val message = buildString {
                append("Message $name is not extendable, but the following extensions are defined: \n")

                extensionsInProject.forEach { ext ->
                    append("-> at ${ext.ctx.toFilePositionString(ext.file.path)}\n")
                }
            }

            throw CompilationException.ExtensionDefinedOnNonExtendableMessage(message, file, ctx)
        }

        extensionRange.validate()

        extensionsInProject.forEach { ext ->
            ext.fields.forEach { extField ->
                if (extField.number !in extensionRange) {
                    val message =
                        """Extension ${extField.name} defined at ${extField.ctx.toFilePositionString(extField.file.path)} 
                            |uses field number ${extField.number}, but only values in ranges ${extensionRange.ranges.map { it.range }} 
                            |are allowed."""
                            .trimMargin()
                    throw CompilationException.ExtensionDefinedOutOfExtensionRange(
                        message,
                        file,
                        ctx
                    )
                }
            }
        }

        extensionsInProject
            .flatMap { it.fields }
            .groupBy { it.number }
            .filter { (_, values) -> values.size > 1 }
            .forEach { (number, values) ->
                val message = buildString {
                    append("Field number $number is used on multiple times on extensions defined for message $name:\n")

                    values.forEach { field ->
                        append("-> ${field.name} at ${field.ctx.toFilePositionString(field.file.path)}")
                    }
                }

                throw CompilationException.FieldNumberConflict(message, file, ctx)
            }
    }
}
