package io.github.timortel.kmpgrpc.plugin.sourcegeneration.util

import org.antlr.v4.runtime.ParserRuleContext

fun ParserRuleContext.toFilePositionString(filePath: String): String {
    val token = start
    return "$filePath:${token.line}:${token.charPositionInLine}"
}
