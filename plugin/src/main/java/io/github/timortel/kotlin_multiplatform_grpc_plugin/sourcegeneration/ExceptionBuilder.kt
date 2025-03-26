package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import org.antlr.v4.runtime.ParserRuleContext

fun ParserRuleContext.toFilePositionString(filePath: String): String {
    val token = start
    return "$filePath:${token.line}:${token.charPositionInLine}"
}
