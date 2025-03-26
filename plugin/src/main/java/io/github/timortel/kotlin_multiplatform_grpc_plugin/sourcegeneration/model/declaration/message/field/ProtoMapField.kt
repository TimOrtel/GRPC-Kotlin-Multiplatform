package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage

data class ProtoMapField(
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    val keyType: ProtoType.MapKeyType,
    val valuesType: ProtoType
) : ProtoBaseField() {
    lateinit var message: ProtoMessage

    val file: ProtoFile get() = message.file

    override val fieldName: String = "${name}Map"

    init {
        keyType.parent = ProtoType.Parent.MapField(this)
        valuesType.parent = ProtoType.Parent.MapField(this)
    }
}
