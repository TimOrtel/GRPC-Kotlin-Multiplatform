package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import org.antlr.v4.runtime.ParserRuleContext

class Protobuf3ParserException(override val message: String?, val ctx: ParserRuleContext) : Exception()
