package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.Protobuf3CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.enumeration.ProtoEnumField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnum(
    override val name: String,
    val fields: List<ProtoEnumField>,
    val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, BaseDeclarationResolver {

    companion object {
        const val UNRECOGNIZED_FIELD_NAME = "UNRECOGNIZED"
    }

    override lateinit var parent: ProtoDeclParent

    override val file: ProtoFile
        get() = when (val p = parent) {
            is ProtoDeclParent.File -> p.file
            is ProtoDeclParent.Message -> p.message.file
        }

    val defaultField: ProtoEnumField
        get() =
            fields
                .firstOrNull { it.number == 0 }
                ?: throw Protobuf3CompilationException("Enumeration does not have field with value 0.", file, ctx)

    init {
        fields.forEach { it.enum = this }
    }

    // An enum does not have children, so it cannot resolve anything
    override fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration? {
        return null
    }
}
