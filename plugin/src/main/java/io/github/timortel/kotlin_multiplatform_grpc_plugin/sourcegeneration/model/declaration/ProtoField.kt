package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration

import org.antlr.v4.runtime.ParserRuleContext

/**
 * A message or enum field.
 */
interface ProtoField {
    val name: String
    val number: Int
    val ctx: ParserRuleContext
}
