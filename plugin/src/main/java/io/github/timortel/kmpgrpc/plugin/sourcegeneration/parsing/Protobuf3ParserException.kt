package io.github.timortel.kmpgrpc.plugin.sourcegeneration.parsing

import org.antlr.v4.runtime.ParserRuleContext

class Protobuf3ParserException(override val message: String?, val ctx: ParserRuleContext) : Exception()