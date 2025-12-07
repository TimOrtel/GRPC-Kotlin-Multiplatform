package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.enumeration

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnumField(
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoField {
    lateinit var enum: ProtoEnum

    override val parentOptionsHolder: ProtoOptionsHolder
        get() = enum

    override val file: ProtoFile
        get() = enum.file

    override val optionTarget: OptionTarget get() = OptionTarget.ENUM_ENTRY
}
