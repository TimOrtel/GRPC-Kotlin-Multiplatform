package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

object ActualRepeatedProtoFieldWriter : RepeatedProtoFieldWriter() {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyListProperty(builder: PropertySpec.Builder, field: ProtoMessageField) {
        builder.initializer("%N", field.codeName)
    }

    override fun modifyCountProperty(builder: PropertySpec.Builder, field: ProtoMessageField) {
        builder.initializer("%N.size", field.codeName)
    }
}
