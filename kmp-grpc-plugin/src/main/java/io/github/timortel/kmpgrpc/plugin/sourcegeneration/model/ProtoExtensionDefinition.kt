package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType

/**
 * Represents the definition of a Protocol Buffers extension.
 *
 * @property messageType The type of the message to which the extension applies.
 * @property fields The list of fields associated with the extension.
 */
data class ProtoExtensionDefinition(
    val messageType: ProtoType.DefType,
    val fields: List<ProtoMessageField>
) {
    lateinit var parent: Parent

    val file: ProtoFile
        get() = parent.file

    init {
        messageType.parent = ProtoType.Parent.ExtensionDefinition(this)
    }

    sealed interface Parent {
        val file: ProtoFile

        data class File(override val file: ProtoFile) : Parent

        data class Message(val message: ProtoMessage) : Parent {
            override val file: ProtoFile
                get() = message.file
        }
    }
}
