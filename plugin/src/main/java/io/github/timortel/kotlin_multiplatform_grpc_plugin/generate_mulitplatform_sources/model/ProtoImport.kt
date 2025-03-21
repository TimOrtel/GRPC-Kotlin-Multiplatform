package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import org.antlr.v4.runtime.ParserRuleContext

data class ProtoImport(val identifier: String, val ctx: ParserRuleContext) {
    // Remove " from identifier
    val path: String = identifier.substring(1, identifier.length - 1)
}
