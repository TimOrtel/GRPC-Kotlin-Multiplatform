package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Represents the definition of a Protocol Buffers extension.
 *
 * @property messageType The type of the message to which the extension applies.
 * @property fields The list of fields associated with the extension.
 */
data class ProtoExtensionDefinition(
    val messageType: ProtoType.DefType,
    val fields: List<ProtoMessageField>,
    val ctx: ParserRuleContext
) : ProtoNode {
    lateinit var parent: Parent

    val file: ProtoFile
        get() = parent.file

    init {
        messageType.parent = ProtoType.Parent.ExtensionDefinition(this)

        fields.forEach { it.parent = ProtoMessageField.Parent.ExtensionDefinition(this) }
    }

    override fun validate() {
        val declType = messageType.declType
        when (declType) {
            ProtoType.DefType.DeclarationType.ENUM -> {
                val message = "Expected extension to reference a message, but instead references an enum."
                throw CompilationException.ExtensionInvalidReference(message, file, ctx)
            }
            ProtoType.DefType.DeclarationType.MESSAGE -> {} // Good case
        }

        fields.forEach { it.validate() }
    }

    sealed interface Parent : DeclarationResolver {
        val file: ProtoFile
        val className: ClassName

        data class File(override val file: ProtoFile) : Parent, DeclarationResolver by file {
            override val className: ClassName
                get() = file.className
        }

        data class Message(val message: ProtoMessage) : Parent, DeclarationResolver by message {
            override val file: ProtoFile
                get() = message.file

            override val className: ClassName
                get() = message.className
        }
    }
}
