package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import org.antlr.v4.runtime.ParserRuleContext

data class ProtoOption(val name: String, val value: String, val ctx: ParserRuleContext)
