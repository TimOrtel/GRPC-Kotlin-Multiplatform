package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import org.antlr.v4.runtime.ParserRuleContext

data class ProtoOption(val name: String, val value: String, val ctx: ParserRuleContext)
