package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoMessageField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    val cardinality: ProtoFieldCardinality,
    override val ctx: ParserRuleContext
) : ProtoRegularField() {

    lateinit var parent: ProtoMessage

    override val file: ProtoFile get() = parent.file

    override val attributeName: String = when(cardinality) {
        ProtoFieldCardinality.Implicit, ProtoFieldCardinality.Optional -> name
        ProtoFieldCardinality.Repeated -> "${name}List"
    }

    init {
        type.parent = ProtoType.Parent.MessageField(this)
    }

    fun defaultValue(): CodeBlock {
        return when (cardinality) {
            ProtoFieldCardinality.Implicit, ProtoFieldCardinality.Optional -> type.defaultValue()
            ProtoFieldCardinality.Repeated -> CodeBlock.of("emptyList()")
        }
    }
}
