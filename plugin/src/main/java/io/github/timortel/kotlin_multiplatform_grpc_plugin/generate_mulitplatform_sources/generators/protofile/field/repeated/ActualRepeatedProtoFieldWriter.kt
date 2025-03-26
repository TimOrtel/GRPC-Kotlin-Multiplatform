package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.repeated

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField

object ActualRepeatedProtoFieldWriter : RepeatedProtoFieldWriter() {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyListProperty(builder: PropertySpec.Builder, field: ProtoMessageField) {
        builder.initializer("%N", Const.Message.Attribute.Repeated.listPropertyName(field))
    }

    override fun modifyCountProperty(builder: PropertySpec.Builder, field: ProtoMessageField) {
        builder.initializer("%N.size", Const.Message.Attribute.Repeated.listPropertyName(field))
    }
}
