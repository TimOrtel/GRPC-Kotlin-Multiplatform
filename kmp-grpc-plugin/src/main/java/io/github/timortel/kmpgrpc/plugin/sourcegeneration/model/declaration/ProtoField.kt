package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Option
import org.antlr.v4.runtime.ParserRuleContext

/**
 * A message or enum field.
 */
interface ProtoField : ProtoOptionsHolder {
    val name: String
    val number: Int
    val ctx: ParserRuleContext

    override val supportedOptions: List<Option<*>> get() = listOf(Options.deprecated)
}
