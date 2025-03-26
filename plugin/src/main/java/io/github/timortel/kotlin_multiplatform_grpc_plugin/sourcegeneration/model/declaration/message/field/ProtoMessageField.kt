package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage

data class ProtoMessageField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    val cardinality: ProtoFieldCardinality
) : ProtoRegularField() {

    lateinit var parent: ProtoMessage

    val file: ProtoFile get() = parent.file

    override val fieldName: String = when(cardinality) {
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
