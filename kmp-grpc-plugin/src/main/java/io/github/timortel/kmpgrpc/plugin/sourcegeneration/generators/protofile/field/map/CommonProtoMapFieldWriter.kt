package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map

import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField

object CommonProtoMapFieldWriter : ProtoMapFieldWriter() {
    override fun modifyMapProperty(builder: PropertySpec.Builder, field: ProtoMapField) = Unit
}
