package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

object CommonRepeatedProtoFieldWriter : RepeatedProtoFieldWriter() {
    override val attrs: List<KModifier> = emptyList()

    override fun modifyListProperty(builder: PropertySpec.Builder, field: ProtoMessageField) = Unit

    override fun modifyCountProperty(builder: PropertySpec.Builder, field: ProtoMessageField) = Unit
}
