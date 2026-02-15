package io.github.timortel.kmpgrpc.plugin.sourcegeneration.parsing

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString
import org.antlr.v4.runtime.ParserRuleContext

class ProtobufParserException(val msg: String?, val ctx: ParserRuleContext, val filePath: String) : Exception() {
    override val message: String
        get() ="${ctx.toFilePositionString(filePath)}: $msg"
}
