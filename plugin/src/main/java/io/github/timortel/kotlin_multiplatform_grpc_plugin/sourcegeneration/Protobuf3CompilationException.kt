package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import org.antlr.v4.runtime.ParserRuleContext

class Protobuf3CompilationException(val msg: String, val filePath: String, val ctx: ParserRuleContext?) : Exception() {
    constructor(message: String, file: ProtoFile, ctx: ParserRuleContext) : this(message, file.fileName, ctx)
    constructor(message: String, file: ProtoFile) : this(message, file.fileName, null)

    override val message: String
        get() = if (ctx == null) {
            "$filePath: $msg"
        } else {
            "${ctx.toFilePositionString(filePath)}: $msg"
        }
}
