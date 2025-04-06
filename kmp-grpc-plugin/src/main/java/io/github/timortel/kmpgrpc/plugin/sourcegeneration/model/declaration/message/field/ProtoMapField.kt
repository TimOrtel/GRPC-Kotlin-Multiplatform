package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoMapField(
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    val keyType: ProtoType.MapKeyType,
    val valuesType: ProtoType,
    override val ctx: ParserRuleContext
) : ProtoBaseField(), ProtoMessageProperty {
    override lateinit var message: ProtoMessage

    override val file: ProtoFile get() = message.file

    override val desiredAttributeName: String = "${name}Map"

    init {
        keyType.parent = ProtoType.Parent.MapField(this)
        valuesType.parent = ProtoType.Parent.MapField(this)
    }
}
