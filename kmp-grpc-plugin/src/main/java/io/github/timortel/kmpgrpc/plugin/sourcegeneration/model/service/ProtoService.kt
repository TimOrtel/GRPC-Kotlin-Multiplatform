package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoBaseDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoService(
    override val name: String,
    val rpcs: List<ProtoRpc>,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoBaseDeclaration, ProtoNode, CodeNameResolver {

    override lateinit var file: ProtoFile

    override val desiredCodeName: String
        get() = "${transformedKotlinName}Stub"

    override val consideredNodes: List<SourceCodeNamedNode>
        get() = rpcs

    override val reservedNames: Set<String>
        get() = emptySet()

    init {
        rpcs.forEach { it.service = this }
    }

    override val optionTarget: OptionTarget = OptionTarget.SERVICE

    override val parentOptionsHolder: ProtoOptionsHolder
        get() = file

    override fun validate() {
        super.validate()

        rpcs.forEach { it.validate() }
    }
}
