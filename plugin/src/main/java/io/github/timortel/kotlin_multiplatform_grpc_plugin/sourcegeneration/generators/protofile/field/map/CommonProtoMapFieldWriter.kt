package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.field.map

import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoMapField

object CommonProtoMapFieldWriter : ProtoMapFieldWriter() {
    override fun modifyMapProperty(builder: PropertySpec.Builder, field: ProtoMapField) = Unit
}
