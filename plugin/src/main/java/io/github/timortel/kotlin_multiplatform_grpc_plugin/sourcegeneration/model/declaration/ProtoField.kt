package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.Options
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOptionsHolder
import org.antlr.v4.runtime.ParserRuleContext

/**
 * A message or enum field.
 */
interface ProtoField : ProtoOptionsHolder {
    val name: String
    val number: Int
    val ctx: ParserRuleContext

    override val supportedOptions: List<Options.Option<*>> get() = emptyList()
}
