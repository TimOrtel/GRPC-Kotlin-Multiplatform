package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf

object CommonProtoOneOfWriter : ProtoOneOfWriter(false) {

    override val attrs: List<KModifier> = emptyList()

    override fun modifyOneOfProperty(
        builder: PropertySpec.Builder,
        oneOf: ProtoOneOf
    ) = Unit
}
