package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import org.antlr.v4.runtime.ParserRuleContext

data class ProtoRange(val range: IntRange, val ctx: ParserRuleContext) {
    operator fun contains(num: Int): Boolean {
        return num in range
    }
}
