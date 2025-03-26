package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoBaseDeclaration
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoService(
    override val name: String,
    val rpcs: List<ProtoRpc>,
    val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoBaseDeclaration {

    override lateinit var file: ProtoFile

    override val kotlinClassName: String = "${name}Stub"

    val jsServiceClassName: ClassName get() = className.nestedClass("JS_$name")

    init {
        rpcs.forEach { it.service = this }
    }
}
