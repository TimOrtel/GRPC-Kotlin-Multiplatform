package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField

object ActualProtoMapFieldWriter : ProtoMapFieldWriter() {

    override fun modifyMapProperty(
        builder: PropertySpec.Builder,
        field: ProtoMapField
    ) {
        builder.initializer(field.codeName)
        builder.addModifiers(KModifier.ACTUAL)
    }
}