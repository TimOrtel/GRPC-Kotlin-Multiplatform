package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.enumeration.ProtoEnumField
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnum(
    override val name: String,
    val fields: List<ProtoEnumField>,
    val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoDeclaration, BaseDeclarationResolver {
    override lateinit var parent: ProtoDeclParent

    override val file: ProtoFile
        get() = when (val p = parent) {
            is ProtoDeclParent.File -> p.file
            is ProtoDeclParent.Message -> p.message.file
        }

    init {
        fields.forEach { it.enum = this }
    }

    // An enum does not have children, so it cannot resolve anything
    override fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration? {
        return null
    }
}
