package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.toFilePositionString
import org.antlr.v4.runtime.ParserRuleContext

sealed class CompilationException(val msg: String, val filePath: String, val ctx: ParserRuleContext?) : Exception() {
    constructor(message: String, file: ProtoFile, ctx: ParserRuleContext) : this(message, file.fileName, ctx)
    constructor(message: String, file: ProtoFile) : this(message, file.fileName, null)

    // General
    class ReservedFieldNumberUsed(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)
    class ReservedFieldNameUsed(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)

    // Imports
    class UnresolvedImport(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)

    // Declaration
    class DuplicateDeclaration(message: String, file: ProtoFile) : CompilationException(message, file.path, null)
    class DuplicatePackageStatement(message: String, filePath: String, ctx: ParserRuleContext) : CompilationException(message, filePath, ctx)

    // Enum
    class EnumIllegalFirstField(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)
    class EnumNoFields(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)

    // Name Resolving
    class ResolvedToPackage(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)
    class ConflictingResolution(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)
    class UnresolvedReference(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)

    // Options
    class OptionFailedParse(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)
    class OptionDuplicateDeclaration(message: String, file: ProtoFile, ctx: ParserRuleContext) : CompilationException(message, file, ctx)

    override val message: String
        get() = if (ctx == null) {
            "$filePath: $msg"
        } else {
            "${ctx.toFilePositionString(filePath)}: $msg"
        }
}
