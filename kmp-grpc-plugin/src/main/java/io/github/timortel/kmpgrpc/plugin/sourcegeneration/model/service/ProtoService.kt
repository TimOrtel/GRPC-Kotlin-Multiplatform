package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoBaseDeclaration
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoService(
    override val name: String,
    val rpcs: List<ProtoRpc>,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoBaseDeclaration, ProtoNode {

    override lateinit var file: ProtoFile

    override val kotlinClassName: String = "${name}Stub"

    val jsServiceClassName: ClassName get() = className.nestedClass("JS_$name")

    init {
        rpcs.forEach { it.service = this }
    }

    override val supportedOptions: List<Options.Option<*>> = emptyList()

    override fun validate() {
        super.validate()

        rpcs.forEach { it.validate() }
    }
}
